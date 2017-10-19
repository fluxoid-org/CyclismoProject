package fluxoid;

import fluxoid.fit.FileType;
import java9.util.Optional;
import java9.util.function.Consumer;
import org.cowboycoders.ant.AntError;
import org.cowboycoders.ant.Channel;
import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.MasterChannelType;
import org.cowboycoders.ant.messages.data.CompleteDataMessage;
import org.cowboycoders.ant.profiles.common.DeviceIds;
import org.cowboycoders.ant.profiles.common.DeviceDescriptor;
import org.cowboycoders.ant.profiles.common.PageDispatcher;
import org.cowboycoders.ant.profiles.fs.Directory;
import org.cowboycoders.ant.profiles.fs.DirectoryHeader;
import org.cowboycoders.ant.profiles.fs.FileEntry;
import org.cowboycoders.ant.profiles.fs.defines.AuthResponseCode;
import org.cowboycoders.ant.profiles.fs.defines.DataType;
import org.cowboycoders.ant.profiles.fs.defines.FileAttribute;
import org.cowboycoders.ant.profiles.fs.defines.DownloadResponseCode;
import org.cowboycoders.ant.profiles.fs.pages.*;
import org.cowboycoders.ant.profiles.fs.pages.behaviours.*;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.DisconnectCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.DownloadCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.LinkCommand;
import org.cowboycoders.ant.profiles.fs.pages.responses.AuthResponse;
import org.cowboycoders.ant.profiles.fs.pages.responses.DownloadResponse;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.cowboycoders.ant.profiles.simulators.NetworkKeys;
import org.cowboycoders.ant.utils.ByteUtils;
import org.fluxoid.utils.Format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FormicaFs {

    private static final Logger logger = Logger.getLogger(FormicaFs.class.getName());

    static {
        logger.setLevel(Level.FINEST);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }


    public interface FileDescriptor {
        byte [] getBytes();
        FileEntry.FileEntryBuilder getFileEntry();
    }

    private static final FileDescriptor [] dirListing = new FileDescriptor[] {
            new FileDescriptor() {
                @Override
                public byte[] getBytes() {
                    return getFit();
                }

                @Override
                public FileEntry.FileEntryBuilder getFileEntry() {
                    URL url = ClassLoader.getSystemResource("test.fit");
                    File file = new File(url.getFile());
                    FileEntry.FileEntryBuilder builder = new FileEntry.FileEntryBuilder();
                    if (file.canRead()) {
                        builder.addAttributes(FileAttribute.READ);
                        builder.setTimeStamp(file.lastModified());
                        builder.setLength((int)file.length());
                    }
                    if (file.canWrite()) {
                        builder.addAttributes(FileAttribute.WRITE, FileAttribute.APPEND, FileAttribute.ERASE);
                    }
                    builder.setDataType(DataType.FIT_FILE);
                    builder.setId(FileType.ACTIVITY.getId());
                    return builder;

                }
            }
    };

    private static class CurrentFile {
        int index; // index in dir
        MultiPart multiPart;

        public CurrentFile(int index, MultiPart multiPart) {
            this.index = index;
            this.multiPart = multiPart;
        }
    }

    private interface StateMachineContext {

        ExecutorService channelExecutor = Executors.newFixedThreadPool(1);

        default void performOffThread(Runnable runnable) {
            channelExecutor.submit(runnable);
        }

        void setStateNow(FsState newState);

        default void setState(FsState newState) {
            performOffThread(() -> setStateNow(newState));
        }

        DeviceDescriptor getDeviceDescriptor();

        Channel getChannel();

        FsState getAuthState();
        FsState getTransportState();
        FsState getAdvertState();

        public void setSerial(int serialNum);

        public int getSerial();

        int getOurSerial();

        AuthBehaviour getPairingBehaviour();

        BeaconAdvert.BeaconPayload getAdvertPayload();

        FsState getState();
    }

    private interface FsState {
        void onTransition(StateMachineContext mutator);
        void handleMessage(StateMachineContext mutator, AntPage page);
    }

    private interface WrappedState<T extends FsState> extends FsState {
        public T getBase();

        @Override
        void onTransition(StateMachineContext mutator);

        @Override
        void handleMessage(StateMachineContext mutator, AntPage page);
    }

    private static class DisconnectDecorator<T extends FsState> implements WrappedState<T> {

        private final T base;

        public T getBase() {
            return base;
        }

        public DisconnectDecorator(T base) {
            this.base = base;
        }

        @Override
        public void onTransition(StateMachineContext mutator) {
            base.onTransition(mutator);
        }

        @Override
        public void handleMessage(StateMachineContext ctx, AntPage page) {
            if (page instanceof DisconnectCommand) {
                logger.info("Received disconnect command");
                ctx.setState(ctx.getAdvertState());
                return;
            }
            base.handleMessage(ctx, page);
        }
    }


    private abstract static class AbstractAdvertState implements FsState {


        @Override
        public void handleMessage(StateMachineContext ctx, AntPage page) {
            if (!(page instanceof LinkCommand)) {
                return;
            }
            LinkCommand linkCommand = (LinkCommand) page;
            ctx.setSerial(linkCommand.getSerialNumber());

            Channel channel = ctx.getChannel();
            channel.setPeriod(4096);
            channel.setFrequency(linkCommand.getRequestedFrequency());

            ctx.setState(ctx.getAuthState());
        }
    }

    private static class InitialAdvertState extends AbstractAdvertState {
        @Override
        public void onTransition(StateMachineContext ctx) {
            byte[] data = new byte[8];

            Channel channel = ctx.getChannel();

            //FIXME: this won't work as it doesn't ensure data is broadcast before being overwritten, but should we
            //       broadcast we are dropping to link
//            ctx.getAdvertPayload().encode(data);
//            channel.setBroadcast(data);
//            data = new byte[8];


            channel.setPeriod(8192);
            channel.setFrequency(50);

            ctx.getAdvertPayload().encode(data);

            channel.setBroadcast(data);
        }
    }


    private static class AdvertState extends AbstractAdvertState {
        @Override
        public void onTransition(StateMachineContext ctx) {
            byte[] data = new byte[8];

            Channel channel = ctx.getChannel();

            ctx.getAdvertPayload().encode(data);

            channel.setBroadcast(data);
        }
    }


    private static class AuthState implements FsState {

        @Override
        public void onTransition(StateMachineContext ctx) {
            Channel channel = ctx.getChannel();
            byte[] beaconData = new byte[8];
            ctx.getPairingBehaviour().onLink(new BeaconAuth.BeaconAuthPayload()
                    .setDataAvailable(true)
                    .setSerialNumber(ctx.getSerial())
            ).encode(beaconData);
            channel.setBroadcast(beaconData);
        }



        @Override
        public void handleMessage(StateMachineContext ctx, AntPage page) {
            if (!(page instanceof AuthCommand)) {
                return;
            }
            AuthCommand authCommand = (AuthCommand) page;
            logger.log(Level.INFO, "authentication request: {0}", authCommand.getMode());
            if (!ctx.getPairingBehaviour().isCmdAcceptable(authCommand)) {
                logger.log(Level.SEVERE, "trying to authenticate with different mode than advertised");
                ctx.setState(ctx.getAdvertState());
                return;
            }
            if (authCommand.getSerialNumber() != ctx.getSerial()) {
                logger.log(Level.SEVERE, "Serial number mismatch");
                ctx.setState(ctx.getAdvertState());
                return;
            }

            logger.log(Level.INFO, "authentication accepted");

            AuthCallback callback = new AuthCallback() {
                @Override
                public void onAccept() {
                    sendResponse(ctx, ctx.getPairingBehaviour().onAcceptAuth(), () -> {
                        System.out.println("onAccept");
                        ctx.setState(ctx.getTransportState());
                    });


                }


                @Override
                public void onReject() {
                    logger.severe("authentication rejected");
                    sendResponse(ctx, reject(), () -> {
                        ctx.setState(ctx.getAdvertState());
                    });

                }
            };

            Optional<AuthResponse> response =  ctx.getPairingBehaviour().onReceieveAuthCmd(authCommand, callback);
            response.stream().forEach((resp) -> {
                sendResponse(ctx, resp, () -> {
                    System.out.println("sent serial");
                    this.onTransition(ctx);
                });
            });
        }

        private AuthResponse reject() {
            return new AuthResponse(AuthResponseCode.REJECT,0, new byte[0]);
        }

        private AuthResponse accept() {
            return new AuthResponse(AuthResponseCode.ACCEPT,0, new byte[0]);
        }


        private void sendResponse(StateMachineContext ctx, AuthResponse response, Runnable next) {
            ctx.performOffThread(() -> {
                Channel channel = ctx.getChannel();
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ctx.getAdvertPayload().encode(os);
                    response.encode(os);

                    channel.sendBurst(os.toByteArray(),60L, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    if (ex instanceof AntError) {
                        ctx.setState(ctx.getAdvertState());
                        return;
                    }
                    throw ex;
                }

                next.run();

            });
        }

    }

    private static byte[] getDirBytes(Directory directory) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        directory.encode(os);
        return os.toByteArray();
    }

    private static class TransportState implements FsState {

        private final FileDescriptor[] dirListing;

        public TransportState(FileDescriptor [] dirListing) {
            this.dirListing = dirListing;
            index = new MultiPart(getDirBytes(mkDir(dirListing)));
        }

        private CurrentFile currentFile;

        private final MultiPart index;

        @Override
        public void onTransition(StateMachineContext ctx) {
            Channel channel = ctx.getChannel();
            // transition back to transport
            byte[] beacon = new byte[8];
            new BeaconTransport.BeaconTransportPayload()
                    .setDataAvailable(true)
                    .setState(CommonBeacon.State.TRANSPORT)
                    .encode(beacon);
            channel.setBroadcast(beacon);

        }

        @Override
        public void handleMessage(StateMachineContext ctx, AntPage page) {
            if (!(page instanceof DownloadCommand)) {
                return;
            }
            DownloadCommand message = (DownloadCommand) page;
            Channel channel = ctx.getChannel();
            switch (message.getIndex()) {
                default: {
                    MultiPart multiPart;
                    if (currentFile != null && currentFile.index == message.getIndex()) {

                    } else {
                        int fileIndex = message.getIndex() -1;
                        if (fileIndex < 0) {
                            logger.severe("ignoring negative index: " +  fileIndex);
                            return;
                        }
                        currentFile = new CurrentFile(message.getIndex(), new MultiPart(dirListing[fileIndex].getBytes()));
                    }
                    multiPart = currentFile.multiPart;
                    logger.info("index: " + message.getIndex() + ", requested");
                    logger.info("offset: " + message.getOffset() + ", requested");
                    logger.info("firstReq: " + message.isFirstRequest());

                    multiPart.setOffset(message.getOffset());

                    if (!message.isFirstRequest()) {
                        // TODO: check crc they provide matches what we have so far
                        logger.info("initial value for crc:" + message.getCrc());
                        multiPart.setExpectedCrc(message.getCrc());
                    }
                    multiPart.accept(channel);
                    if (multiPart.hasFailed()) { // sending file failed
                        // possible reasons for failure : crc mismatch, burst failed
                        // TODO: try with smaller chunks for burst failure?
                        ctx.setState(ctx.getTransportState());
                        logger.warning("failed with reason: " + multiPart.getFailure().get());
                    }
                    break;
                }
                case 0: {
                    index.setOffset(message.getOffset());
                    index.accept(channel);
                    break;
                }


            }
            onTransition(ctx);
        }


    }


    private static class StateMachineContextImpl implements StateMachineContext {

        private final int ourSerial;
        private Channel channel;
        private final AuthBehaviour pairingBehaviour;
        private final DeviceDescriptor deviceDescriptor;
        private final Node node;

        private InitialAdvertState advertState = new InitialAdvertState();
        private WrappedState<AuthState> authState = new DisconnectDecorator<>(new AuthState());
        private final WrappedState<TransportState> transportState;

        private FsState state = getAdvertState();

        public StateMachineContextImpl(Node node, AuthBehaviour pairingBehaviour, DeviceDescriptor deviceDescriptor, int ourSerial, FileDescriptor[] dirListing) {
            this.pairingBehaviour = pairingBehaviour;
            this.ourSerial = ourSerial;
            this.node = node;
            this.deviceDescriptor = deviceDescriptor;
            transportState = new DisconnectDecorator<>(new TransportState(dirListing));
        }

        @Override
        public void setStateNow(FsState newState) {
            if (state != newState) {
                newState.onTransition(this);
            }
            state = newState;
        }

        @Override
        public DeviceDescriptor getDeviceDescriptor() {
            return deviceDescriptor;
        }

        @Override
        public Channel getChannel() {
            if (channel == null) {
                node.start();
                channel = node.getFreeChannel();
            }
            return channel;
        }

        @Override
        public FsState getAuthState() {
            return authState;
        }

        @Override
        public FsState getTransportState() {
            return transportState;
        }

        @Override
        public FsState getAdvertState() {
            return advertState;
        }

        private boolean serialSet = false;
        private Integer serial = null;

        @Override
        public void setSerial(int serialNum) {
            if(serialSet) {
                // serial should be locked in for rest of transaction
                return;
            }
            serial = serialNum;
            serialSet = true;
        }

        @Override
        public int getSerial() {
            return serial;
        }

        @Override
        public int getOurSerial() {
            return ourSerial;
        }

        @Override
        public AuthBehaviour getPairingBehaviour() {
            return pairingBehaviour;
        }

        @Override
        public BeaconAdvert.BeaconPayload getAdvertPayload() {
            return new BeaconAdvert.BeaconPayload()
                    .setFsDeviceType(deviceDescriptor.getDeviceId())
                    .setManufacturerID(deviceDescriptor.getManufacturerId())
                    .setDataAvailable(true);
        }

        @Override
        public FsState getState() {
            return state;
        }
    }


    public void start(StateMachineContext ctx) {

        final Channel channel = ctx.getChannel();


        ChannelType type = new MasterChannelType(false, false);
        channel.assign(NetworkKeys.ANT_FS, type);
        // deviceNumber can be set to anything (not whitelisted)
        channel.setId(1235, DeviceIds.Garmin.manufacturerId(), 0, false);

        final PageDispatcher dispatcher = new PageDispatcher();

        channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);

        ctx.getAdvertState().onTransition(ctx);

        channel.registerRxListener(msg -> {

            byte[] data1 = msg.getPrimitiveData();
            if (!dispatcher.dispatch(data1)) {

            }
            logger.fine(Format.bytesToString(data1).toString());

        }, CompleteDataMessage.class);

        channel.registerBurstListener(message -> dispatcher.dispatch(ByteUtils.unboxArray(message.getData())));

        dispatcher.addListener(AntPage.class, (msg) -> {
            // need to perform this out of dispatcher thread as this is shared for replies to channel messages
            ctx.performOffThread(() -> ctx.getState().handleMessage(ctx, msg));
        });

        channel.open();
    }

    public static void main(String[] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);

        final int ourSerial = 0xdeadbeed;
        final byte [] passkey = new byte [] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef, 1,2,3,4};


        AuthInfo info = new AuthInfo() {
            public int getSerial() {
                return ourSerial;
            }

            public byte[] getPasskey() {
                return passkey;
            }
        };

        PairingBehaviour pair = new PairingBehaviour(info) {

            @Override
            public Optional<AuthResponse> onReceieveAuthCmd(AuthCommand cmd, AuthCallback callback) {
                System.out.print("pairing request from " );
                System.out.printf("%x\n", cmd.getSerialNumber());
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("simulating user accept");
                        callback.onAccept();
                    }
                },500);
                return Optional.empty();
            }

        };

        PasskeyBehaviour passBehaviour = new PasskeyBehaviour(info);

        AuthBehaviour behaviour = new CombinedPasskeyBehaviour(pair, passBehaviour, info);

        StateMachineContext ctx = new StateMachineContextImpl(node, behaviour, DeviceIds.Garmin.FR70, ourSerial, dirListing);


        //DeviceIds.Garmin.FR70

        new FormicaFs().start(ctx);

    }

    public static Directory mkDir(FileDescriptor[] dirListing) {
        int modStamp = -1;
        DirectoryHeader.DirectoryHeaderBuilder header = new DirectoryHeader.DirectoryHeaderBuilder()
                .setSystemTimestamp(new GregorianCalendar());

        Directory.DirectoryBuilder dir = new Directory.DirectoryBuilder()
                .setHeader(header);

        int index = 1;
        for (FileDescriptor desc: dirListing) {
            FileEntry.FileEntryBuilder entry = desc.getFileEntry();
            dir.addFile(entry.setIndex(index++).create());
            if (entry.getTimeStamp() > modStamp) {
                modStamp = entry.getTimeStamp();
            }
        }

        header.setModifiedTimestamp(modStamp);
        dir.setHeader(header);


        return dir.create();
    }

    public static byte[] getFit() {
        // should probably use a stream rather than loading into memory
        byte[] data = new byte[0];
        try (InputStream resource = ClassLoader.getSystemResourceAsStream("test.fit");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int avail;
            while ((avail = resource.available()) > 0) {
                out.write(resource.read());
            }
            data = out.toByteArray();

        } catch (IOException e) {
            // return zero length array
        }
        return data;
    }


    public interface FailureReason {}

    public static class TransferException implements FailureReason {
        private final Exception exception;

        TransferException(Exception e) {
            this.exception = e;
        }

        public Exception getException() {
            return exception;
        }
    }

    public static class ErrorResponse implements FailureReason {
        private final DownloadResponseCode downloadResponseCode;

        ErrorResponse(DownloadResponseCode downloadResponseCode) {
            this.downloadResponseCode = downloadResponseCode;
        }

        public DownloadResponseCode getDownloadResponseCode() {
            return downloadResponseCode;
        }
    }

    private static class MultiPart implements Consumer<Channel> {

        private final int chunkSize;
        private Optional<FailureReason> failure = Optional.empty();
        private int expectedCrc = 0;

        public boolean hasFailed() {
            return failure.isPresent();
        }

        public Optional<FailureReason> getFailure() {
            return failure;
        }

        private MultiPart(byte[] fullFile) {
            this(fullFile, DEFAULT_CHUNK_SIZE);
        }

        private MultiPart(byte[] fullFile, int chunkSize) {
            this.chunkSize = chunkSize;
            this.full = fullFile;
        }

        public void setOffset(int offset) {
            this.offset = offset;
            if (offset == 0) {
                expectedCrc = 0;
                crc = 0;
            }
        }

        private final byte[] full;
        private static final int DEFAULT_CHUNK_SIZE = 512;
        private int offset = 0;
        private int crc = 0;

        public void accept(Channel channel) {
            try {
                if (full.length <= offset) {
                    logger.severe("offset out of bounds");
                    return;
                }
                DownloadResponseCode downloadResponseCode = DownloadResponseCode.NO_ERROR;

                if (expectedCrc != crc) {
                    logger.finer("offset: " + offset);
                    logger.warning("expected crc: " + expectedCrc + ", our: " + crc);
                    downloadResponseCode = DownloadResponseCode.CRC_MISMATCH;
                }

                DownloadResponse response = new DownloadResponse(downloadResponseCode, full, chunkSize, offset, expectedCrc);
                crc = response.getPayloadCrc();

                ByteArrayOutputStream os = new ByteArrayOutputStream(response.getLength());
                response.encode(os);

                byte [] data = os.toByteArray();
                channel.sendBurst(data, 60L, TimeUnit.SECONDS);
                logger.finest("sent fit file");

                if (downloadResponseCode != DownloadResponseCode.NO_ERROR) {
                    failure = Optional.of(new ErrorResponse(downloadResponseCode));
                }

            } catch (Exception e) {
                e.printStackTrace();
                failure = Optional.of(new TransferException(e));
            }

        }

        public void setExpectedCrc(int expectedCrc) {
            this.expectedCrc = expectedCrc;
        }
    }


}
