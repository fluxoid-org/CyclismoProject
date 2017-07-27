package org.cowboycoders.ant.profiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cowboycoders.ant.*;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.SlaveChannelType;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;
import org.cowboycoders.ant.profiles.simulators.NetworkKeys;

import java.util.ArrayList;

/**
 * Created by fluxoid on 02/07/17.
 */
public class EliteAntProfile {

    private Channel channel;
    private long totalDistance = -1;
    private long prevDistance;

    public static void main(String [] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        node.start();
        node.reset();
        new EliteAntProfile().start(node);
    }


    private Logger logger = LogManager.getLogger();

    /**
     *
     * @param percent (maybe an absolute value)
     * @return
     */
    private static byte [] genResistance(int percent) {
        byte [] payload = new byte[8];
        payload[0] = (byte) 0xff;
        payload[1] = (byte) 0xff;
        payload[2] = (byte) percent;
        payload[3] = payload[4] = payload[5] = payload[6] = 0;
        payload[7] = 1;
        return payload;
    }

    private static byte [] genPower(int pow) {
        byte [] payload = new byte[8];
        payload[0] = (byte) pow;
        payload[1] = (byte) (pow >>> 8);
        payload[2] = payload[3] = payload[4] = payload[5] = payload[6] = 0;
        payload[7] = 1;
        return payload;
    }

    private ArrayList<BroadcastDataMessage> msgQueue = new ArrayList<>();
    BroadcastDataMessage pending = null;

    public void setPower(int target) {
        BroadcastDataMessage payload = new BroadcastDataMessage();
        payload.setData(genPower(target));
        msgQueue.add(payload);
    }

    public void setResistance(int target) {
        BroadcastDataMessage payload = new BroadcastDataMessage();
        payload.setData(genResistance(target));
        msgQueue.add(payload);
    }


    public void start(Node transceiver) {

        setResistance(50);

        channel = transceiver.getFreeChannel();
        ChannelType type = new SlaveChannelType(false, false);
        channel.assign(NetworkKeys.ANT_SPORT, type);
        channel.setId(ChannelId.WILDCARD,121, ChannelId.WILDCARD, false);
        channel.setPeriod(16172);
        channel.setFrequency(57);

//        // does it transmit power?! (this doesn't work by the way)
//        Channel channel2 = transceiver.getFreeChannel();
//        channel2.assign(NetworkKeys.ANT_SPORT, type);
//        channel2.setId(ChannelId.WILDCARD,11, ChannelId.WILDCARD, false);
//        channel2.setFrequency(57);
//        channel2.setPeriod(8182);
//        channel2.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);

        channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);
        channel.registerRxListener(new BroadcastListener<BroadcastDataMessage>() {

            @Override
            public void receiveMessage(BroadcastDataMessage msg) {
                final byte [] data = msg.getPrimitiveData();
                final int speedLsb = 0xff & data[0];
                final int speedMsb = 0xff & data[1];
                final int cadenceLsb = 0xff &  data[4];
                final int cadenceMsb = 0xff & data[5];
                // speed too low?
                final boolean outofRange = data[6] == (byte) 0xff;
                final int pageNum = data[7] & 0xff; // best guess

                double speed = (speedLsb >> 4) + (speedLsb & 0xf) * 0.1 + (speedMsb >> 4) * 10 + (speedMsb & 0xf) * 100;
                double cadence = (cadenceLsb >> 4) + (cadenceMsb >> 4) * 10 + (cadenceMsb) * 100;
                final int distanceMsb = 0xff & data[3];
                final int distanceLsb = 0xff & data[2];
                final int distance = (distanceMsb << 9) + (distanceLsb << 1);
                if (totalDistance == -1) {
                    totalDistance = -distance;
                }
                if (distance < prevDistance) {
                    // rolled over
                    totalDistance += 2 << 16;
                }
                totalDistance += distance - prevDistance;
                prevDistance = distance;
                if (speed > 0.0) System.out.println("speed: " + speed);
                if (totalDistance > 0) System.out.println("distance: " + totalDistance);
                if (cadence > 0.0) System.out.println("cadence: " + cadence);
                if (msgQueue.size() > 0 ) {
                    BroadcastDataMessage next = msgQueue.get(0);
                    if (next != pending) {
                        pending = next;
                        channel.send(next);
                    }

                }

            }
        }, BroadcastDataMessage.class);

        channel.registerEventHandler(new ChannelEventHandler() {
            @Override
            public void onTransferNextDataBlock() {

            }

            @Override
            public void onTransferTxStart() {

            }

            @Override
            public void onTransferTxCompleted() {

            }

            @Override
            public void onTransferTxFailed() {

            }

            @Override
            public void onChannelClosed() {

            }

            @Override
            public void onRxFailGoToSearch() {

            }

            @Override
            public void onChannelCollision() {

            }

            @Override
            public void onTransferRxFailed() {

            }

            @Override
            public void onRxSearchTimeout() {

            }

            @Override
            public void onRxFail() {

            }

            @Override
            public void onTxSuccess() {
                System.out.println("sending");
                msgQueue.remove(0);
            }
        });

        channel.open();
        //channel2.open();

    }
}
