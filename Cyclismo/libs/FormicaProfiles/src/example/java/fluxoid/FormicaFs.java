package fluxoid;

import fluxoid.fit.FileType;
import java9.util.Optional;
import java9.util.function.Consumer;
import org.cowboycoders.ant.Channel;
import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.MasterChannelType;
import org.cowboycoders.ant.messages.data.CompleteDataMessage;
import org.cowboycoders.ant.profiles.common.PageDispatcher;
import org.cowboycoders.ant.profiles.fs.Directory;
import org.cowboycoders.ant.profiles.fs.DirectoryHeader;
import org.cowboycoders.ant.profiles.fs.FileEntry;
import org.cowboycoders.ant.profiles.fs.defines.DataType;
import org.cowboycoders.ant.profiles.fs.defines.FileAttribute;
import org.cowboycoders.ant.profiles.fs.defines.ResponseCode;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAdvert;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAuth;
import org.cowboycoders.ant.profiles.fs.pages.BeaconTransport;
import org.cowboycoders.ant.profiles.fs.pages.CommonBeacon;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.DisconnectCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.DownloadCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.LinkCommand;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FormicaFs {

    // these represent manufacturers of watches
    private static final int GARMIN = 1;
    private static final int HEALTH_AND_LIFE = 257;
    public static final int GARMIN_FR70_DEV_ID = 1436;
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

    private final FileDescriptor [] dirListing = new FileDescriptor[] {
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

    private interface StateMutator {

        ExecutorService channelExecutor = Executors.newFixedThreadPool(1);

        default void performOffThread(Runnable runnable) {
            channelExecutor.submit(runnable);
        }

        void setStateNow(FsState newState);

        default void setState(FsState newState) {
            performOffThread(() -> setStateNow(newState));
        }

        Channel getChannel();

        AuthState getAuthState();
        TransportState getTransportState();
        AdvertState getAdvertState();
    }

    private interface FsState {
        void onTransition(StateMutator mutator);
        void handleMessage(StateMutator mutator, AntPage page);
    }

    private interface WrappedState<T extends FsState> {
        public T getBase();
    }

    private static class DisconnectDecorator<T extends FsState> implements FsState, WrappedState<T> {

        private final T base;

        public T getBase() {
            return base;
        }

        public DisconnectDecorator(T base) {
            this.base = base;
        }

        @Override
        public void onTransition(StateMutator mutator) {
            base.onTransition(mutator);
        }

        @Override
        public void handleMessage(StateMutator ctx, AntPage page) {
            if (page instanceof DisconnectCommand) {
                logger.info("Received disconnect command");
                ctx.setState(ctx.getAdvertState());
                return;
            }
            base.handleMessage(ctx, page);
        }
    }

    // advertise as garmin fr70 (there is a whitelist of devices)
    private AdvertState advertState = new AdvertState(1, GARMIN_FR70_DEV_ID);
    private WrappedState<AuthState> authState = new DisconnectDecorator<>(new AuthState());
    private WrappedState<TransportState>  transportState = new DisconnectDecorator<>(new TransportState(dirListing));

    private static class AdvertState implements FsState {

        private final int manufacturerId;
        private final int deviceType;

        public AdvertState(int manufacturerId, int deviceType) {
            this.manufacturerId = manufacturerId;
            this.deviceType = deviceType;
        }

        @Override
        public void onTransition(StateMutator mutator) {
            Channel channel = mutator.getChannel();
            channel.setPeriod(8192);
            channel.setFrequency(50);
            byte[] data = new byte[8];

            new BeaconAdvert.BeaconPayload()
                    .setFsDeviceType(deviceType)
                    .setManufacturerID(manufacturerId)
                    .setDataAvailable(true)
                    .encode(data);

            channel.setBroadcast(data);
        }
        public AuthCommand.AuthMode getAuthMode() {
            return AuthCommand.AuthMode.PASSTHROUGH;
        }

        @Override
        public void handleMessage(StateMutator ctx, AntPage page) {
            if (!(page instanceof LinkCommand)) {
                return;
            }
            LinkCommand linkCommand = (LinkCommand) page;
            Channel channel = ctx.getChannel();
            byte[] beaconData = new byte[8];
            new BeaconAuth.BeaconAuthPayload()
                    .setDataAvailable(true)
                    .setSerialNumber(linkCommand.getSerialNumber())
                    .encode(beaconData);
            beaconData[3] = (byte) AuthCommand.AuthMode.PASSTHROUGH.ordinal();
            logger.info(Format.format("serial: %x\n", linkCommand.getSerialNumber()));
            channel.setBroadcast(beaconData);
            channel.setPeriod(4096);
            channel.setFrequency(linkCommand.getRequestedFrequency());
            logger.fine("made link transition");
            ctx.setState(ctx.getAuthState());
        }
    }


    private static class AuthState implements FsState {

        @Override
        public void onTransition(StateMutator ctx) {

        }

        @Override
        public void handleMessage(StateMutator ctx, AntPage page) {
            if (!(page instanceof AuthCommand)) {
                return;
            }
            AuthCommand authCommand = (AuthCommand) page;
            logger.log(Level.INFO, "authentication request: {0}", authCommand.getMode());
            if (((AuthCommand) page).getMode() != ctx.getAdvertState().getAuthMode()) {
                logger.log(Level.SEVERE, "trying to authenticate with different mode than advertised");
                ctx.setState(ctx.getAdvertState());
                return;
            }
            switch (authCommand.getMode()) {
                case PASSTHROUGH:
                        ctx.setState(ctx.getTransportState());
                    break;
                default:
                    logger.severe("should handle other auth states");


            }
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
        public void onTransition(StateMutator ctx) {
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
        public void handleMessage(StateMutator ctx, AntPage page) {
            if (!(page instanceof DownloadCommand)) {
                return;
            }
            DownloadCommand message = (DownloadCommand) page;
            Channel channel = ctx.getChannel();
            switch (message.getIndex()) {
                default: {
                    MultiPart multiPart;
                    if (currentFile != null && currentFile.index == message.getIndex()) {
                        // do nothing
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
                    if (!message.isFirstRequest()) {
                        // TODO: check crc they provide matches what we have so far
                        logger.info("initial value for crc:" + message.getCrc());
                        multiPart.setExpectedCrc(message.getCrc());
                    }
                    multiPart.setOffset(message.getOffset());
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
                    index.accept(channel);
                    break;
                }


            }
            onTransition(ctx);
        }


    }


    private StateMutator stateMutator;
    private FsState state = advertState;

    private class StateMutatorImpl implements StateMutator {

        private final Channel channel;

        public StateMutatorImpl(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void setStateNow(FsState newState) {
            if (state != newState) {
                newState.onTransition(this);
            }
            state = newState;
        }


        @Override
        public Channel getChannel() {
            return channel;
        }

        @Override
        public AuthState getAuthState() {
            return authState.getBase();
        }

        @Override
        public TransportState getTransportState() {
            return transportState.getBase();
        }

        @Override
        public AdvertState getAdvertState() {
            return advertState;
        }
    }


    public void start(Node node) {
        node.start();
        node.reset();

        final Channel channel = node.getFreeChannel();

         stateMutator = new StateMutatorImpl(channel);

        ChannelType type = new MasterChannelType(false, false);
        channel.assign(NetworkKeys.ANT_FS, type);
        // deviceNumber can be set to anything (not whitelisted)
        channel.setId(1234, GARMIN, 0, false);

        final PageDispatcher dispatcher = new PageDispatcher();

        channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);

        advertState.onTransition(stateMutator);

        channel.registerRxListener(msg -> {

            byte[] data1 = msg.getPrimitiveData();
            if (!dispatcher.dispatch(data1)) {

            }
            logger.fine(Format.bytesToString(data1).toString());

        }, CompleteDataMessage.class);

        channel.registerBurstListener(message -> dispatcher.dispatch(ByteUtils.unboxArray(message.getData())));

        dispatcher.addListener(AntPage.class, (msg) -> {
            // need to perform this out of dispatcher thread as this is shared for replies to channel messages
            stateMutator.performOffThread(() -> state.handleMessage(stateMutator, msg));
        });

        channel.open();
    }

    public static void main(String[] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        new FormicaFs().start(node);

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
        private final ResponseCode responseCode;

        ErrorResponse(ResponseCode responseCode) {
            this.responseCode = responseCode;
        }

        public ResponseCode getResponseCode() {
            return responseCode;
        }
    }

    private static class MultiPart implements Consumer<Channel> {

        private final int chunkSize;
        private Optional<FailureReason> failure = Optional.empty();
        private int expectedCrc;

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
                crc = 0;
                expectedCrc = 0;
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
                ResponseCode responseCode = ResponseCode.NO_ERROR;
                if (expectedCrc != crc) {
                    responseCode = ResponseCode.CRC_MISMATCH;
                }
                DownloadResponse response = new DownloadResponse(responseCode, full, chunkSize, offset, crc);
                crc = response.getPayloadCrc();

                ByteArrayOutputStream os = new ByteArrayOutputStream(response.getLength());
                response.encode(os);

                byte [] data = os.toByteArray();
                channel.sendBurst(data, 60L, TimeUnit.SECONDS);
                logger.finest("sent fit file");

                if (responseCode != ResponseCode.NO_ERROR) {
                    failure = Optional.of(new ErrorResponse(responseCode));
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
