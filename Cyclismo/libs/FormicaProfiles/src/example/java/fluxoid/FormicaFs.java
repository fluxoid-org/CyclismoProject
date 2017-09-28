package fluxoid;

import org.cowboycoders.ant.Channel;
import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.MasterChannelType;
import org.cowboycoders.ant.messages.data.DataMessage;
import org.cowboycoders.ant.profiles.common.PageDispatcher;
import org.cowboycoders.ant.profiles.fs.Directory;
import org.cowboycoders.ant.profiles.fs.DirectoryHeader;
import org.cowboycoders.ant.profiles.fs.FileEntry;
import org.cowboycoders.ant.profiles.fs.defines.FileAttribute;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAdvert;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAuth;
import org.cowboycoders.ant.profiles.fs.pages.BeaconTransport;
import org.cowboycoders.ant.profiles.fs.pages.CommonBeacon;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.DownloadCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.LinkCommand;
import org.cowboycoders.ant.profiles.simulators.NetworkKeys;
import org.fluxoid.utils.Format;
import org.fluxoid.utils.bytes.LittleEndianArray;
import org.fluxoid.utils.crc.Crc16Utils;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FormicaFs {

    // these represent manufacturers of watches
    private static final int GARMIN = 1;
    private static final int HEALTH_AND_LIFE = 257;
    public static final int GARMIN_FR70_DEV_ID = 1436;

    private Runnable runMe = new Runnable() {
        @Override
        public void run() {

        }
    };

    enum State {
        LINK,
        AUTH,
        TRANSPORT
    }


    public static void main(String [] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        node.start();
        node.reset();

        final ExecutorService channelExecutor = Executors.newFixedThreadPool(1);

        final Channel channel = node.getFreeChannel();
        ChannelType type = new MasterChannelType(false, false);
        channel.assign(NetworkKeys.ANT_FS, type);
        channel.setId(GARMIN_FR70_DEV_ID, GARMIN,0,false);
        channel.setPeriod(8192);
        channel.setFrequency(50);
        byte [] data = new byte[8];

        new BeaconAdvert.BeaconPayload()
                .setFsDeviceType(1436)
                .setManufacturerID(1)
                .setDataAvailable(true)
                .encode(data);

        final PageDispatcher dispatcher = new PageDispatcher();

        channel.setBroadcast(data);
        channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);
        channel.registerRxListener(new BroadcastListener<DataMessage>() {
            @Override
            public void receiveMessage(DataMessage msg) {
                byte [] data = msg.getPrimitiveData();
                if (!dispatcher.dispatch(data)) {

                }
                System.out.println(Format.bytesToString(data));

            }
        }, DataMessage.class);

        dispatcher.addListener(LinkCommand.class, new BroadcastListener<LinkCommand>() {
            @Override
            public void receiveMessage(final LinkCommand message) {
                        // possibly check serial number?
                        channelExecutor.submit(new Runnable() {
                            @Override
                            public void run() {
                                byte [] data = new byte[8];
                                new BeaconAuth.BeaconAuthPayload()
                                        .setDataAvailable(true)
                                        .setSerialNumber(message.getSerialNumber())
                                        .encode(data);
                                data[3] = (byte) AuthCommand.AuthMode.PASSTHROUGH.ordinal();
                                System.out.printf("serial: %x\n", message.getSerialNumber());
                                channel.setBroadcast(data);
                                channel.setPeriod(4096);
                                channel.setFrequency(message.getRequestedFrequency());
                                System.out.println("made transisiton");
                            }
                        });

                }
        });

        dispatcher.addListener(AuthCommand.class, new BroadcastListener<AuthCommand>() {
            @Override
            public void receiveMessage(final AuthCommand message) {
                System.out.println(message.getMode());
                switch (message.getMode()) {
                    case PASSTHROUGH:
                        // burst might not necessary
                        final byte [] data = new byte[16];
                        data[0] = 67; // beacon advert
                        data[8] = 68;
                        data[9] = (byte) 0x84;
                        data[10] = 1; // accept auth
                        channelExecutor.submit(new Runnable() {
                            @Override
                            public void run() {
//                                try {
//                                    channel.sendBurst(data,2L, TimeUnit.SECONDS);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                } catch (TimeoutException e) {
//                                    e.printStackTrace();
//                                }
//                                System.out.println("sent burst");
                                byte [] data = new byte[8];
                                new BeaconTransport.BeaconTransportPayload()
                                        //.setSerialNumber(0xa56bde7b) // android app
                                        .setDataAvailable(true)
                                        .setState(CommonBeacon.State.TRANSPORT)
                                        .encode(data);
                                channel.setBroadcast(data);
                            }
                        });

                }

            }
        });


        Runnable downloadIndexBehaviour = new Runnable() {

            @Override
            public void run() {
                byte [] data = new byte[64]; // pad to multiple of 8 bytes
                LittleEndianArray view = new LittleEndianArray(data);
                data[0] = 67;
                data[2] = 3; // guess
                data[8] = 68;
                data[9] = (byte) 0x89; // download response codetS
                data[10] = 0; // response code - zero = no error?
                view.put(12,4,32); // remaining data
                view.put(16,4,0); // offset of data
                view.put(20,4,32); // file size;
                ByteBuffer buf = ByteBuffer.wrap(data);
                buf.position(24);
                buf = buf.slice();



                mkDir().accept(buf);

                System.out.println("before crc");
                byte [] cp = Arrays.copyOfRange(data, 24, 56);
                int crc = Crc16Utils.computeCrc(Crc16Utils.ANSI_CRC16_TABLE, 0, cp);
                System.out.println("crc: " + crc);
                view.put(data.length - 2,2, crc);

                channel.sendBurst(data,10L, TimeUnit.SECONDS);
                System.out.println("sent file info");
            }
        };


        //TODO: we need to listen to burst rather than these individual messages
        dispatcher.addListener(DownloadCommand.class, new BroadcastListener<DownloadCommand>() {
            @Override
            public void receiveMessage(DownloadCommand message) {
                switch (message.getIndex()) {
                    default: {
                        System.out.println("index: " + message.getIndex() + ", requested");
                        break;
                    }
                    case 0: {
                        channelExecutor.submit(downloadIndexBehaviour);
                        break;
                    }


                }
                channelExecutor.submit(new Runnable() {
                    @Override
                    public void run() {

                        // transition back to transport
                        byte [] beacon = new byte[8];
                        new BeaconTransport.BeaconTransportPayload()
                                //.setSerialNumber(0xa56bde7b) // android app
                                .setDataAvailable(true)
                                .setState(CommonBeacon.State.TRANSPORT)
                                .encode(beacon);
                        channel.setBroadcast(beacon);

                        //byte [] beacon = new byte[8];
                        //new BeaconTransport.BeaconTransportPayload()
                        ///       .encode(beacon);
                        //channel.setBroadcast(beacon);
                    }
                });
            }
        });




        channel.open();
    }

    public static Directory mkDir() {
        DirectoryHeader.DirectoryHeaderBuilder header = new DirectoryHeader.DirectoryHeaderBuilder()
                .setModifiedTimestamp(100)
                .setSystemTimestamp(200);

        FileEntry soleEntry = new FileEntry.FileEntryBuilder()
                .addAttributes(FileAttribute.READ)
                .setIndex(1) // don't set to zero
                .setLength(8)
                .setId(4) // for watch file type id
                .setTimeStamp(150)
                .setDataType(128) // fit file
                .create();

        Directory dir = new Directory.DirectoryBuilder()
                .setHeader(header)
                .addFile(soleEntry)
                .create();
        return dir;
    }
}
