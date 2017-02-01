package org.org.cowboycoders.ant.profiles.simulators;

import org.cowboycoders.ant.*;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.events.MessageCondition;
import org.cowboycoders.ant.events.MessageConditionFactory;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.MasterChannelType;
import org.cowboycoders.ant.messages.MessageId;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;
import org.cowboycoders.ant.messages.data.DataMessage;
import org.cowboycoders.ant.messages.responses.Response;
import org.cowboycoders.ant.messages.responses.ResponseCode;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.BikeData;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.GeneralData;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.cowboycoders.ant.events.MessageConditionFactory.newResponseCondition;

/**
 * Created by fluxoid on 31/01/17.
 */
public class Dummy {

    private Node transceiver;

    public static void printBytes(Byte[] arr) {
        for (byte b: arr) {
            System.out.printf("%02x:", b);
        }
        System.out.println();
    }

    Timer timer = new Timer();


    public static void main(String [] args) {
        //printBytes(new Byte[] {-1,2,3});
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        node.start();
        node.reset();
        new Dummy().start(node);
    }

    public void start(Node transceiver) {
        this.transceiver = transceiver;



        ExecutorService pool = Executors.newSingleThreadExecutor();

        DummyTrainerState state = new DummyTrainerState();

        state.setPower(200);
        state.setCadence(75);
        state.setHeartRate(123);
        state.setSpeed(new BigDecimal(10));



        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                state.incrementLaps();

            }
        }, 1000, 60000);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int distance = state.getDistance();
                state.setDistance(distance += 1);
            }
        }, 1000, 1000);

        Channel channel = transceiver.getFreeChannel();
        ChannelType type = new MasterChannelType(false, false);
        channel.assign(NetworkKeys.ANT_SPORT, type);
        channel.setId(1234,17,255,false);
        channel.setPeriod(8192);
        channel.setFrequency(57);
        channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);
        channel.registerRxListener(new BroadcastListener<DataMessage>() {
            @Override
            public void receiveMessage(DataMessage msg) {
                printBytes(msg.getData());
            }
        }, DataMessage.class);

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
                BroadcastDataMessage msg = new BroadcastDataMessage();
                msg.setData(state.nextPacket());
                channel.send(msg);
            }
        });

        channel.open();

    }
}
