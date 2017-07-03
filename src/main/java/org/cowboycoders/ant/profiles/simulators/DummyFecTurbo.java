package org.cowboycoders.ant.profiles.simulators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cowboycoders.ant.Channel;
import org.cowboycoders.ant.ChannelEventHandler;
import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.MasterChannelType;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;
import org.cowboycoders.ant.messages.data.DataMessage;
import org.cowboycoders.ant.profiles.common.PageDispatcher;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines.CommandId;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.*;
import org.cowboycoders.ant.profiles.pages.Request;
import org.fluxoid.utils.Format;

import java.util.Timer;
import java.util.TimerTask;

import static org.cowboycoders.ant.profiles.common.PageDispatcher.getPageNum;

/**
 * Created by fluxoid on 31/01/17.
 */
public class DummyFecTurbo implements TurboControllable {

    private static final Logger logger = LogManager.getLogger();

    private Timer timer = new Timer();

    private final FecTurboState state = new FecTurboState();


    public static void main(String [] args) {
        //bytesToString(new Byte[] {-1,2,3});
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        node.start();
        node.reset();
        new DummyFecTurbo().start(node);
    }

    @Override
    public void start(Node transceiver) {

        state.setPower(200);
        state.setHeartRate(123);

        final PageDispatcher pageDispatcher = new PageDispatcher();

        pageDispatcher.addListener(Request.class, new BroadcastListener<Request>() {

            @Override
            public void receiveMessage(Request request) {
                final int page = request.getRequestedPageNumber();
                switch (page) {
                    case CapabilitiesPage.PAGE_NUMBER:
                        logger.trace("capabilities requested");
                        state.setCapabilitesRequested();
                        break;
                    case ConfigPage.PAGE_NUMBER:
                        logger.trace("config requested");
                        state.setConfigRequested();
                        break;
                    case Command.PAGE_NUMBER:
                        logger.trace("command status requested");
                        state.sendCmdStatus();
                        break;
                    case CalibrationResponse.PAGE_NUMBER:
                        logger.trace("calibration response requested");
                        state.sendCalibrationResponse();
                        break;
                    case PercentageResistance.PAGE_NUMBER:
                        logger.trace("requested basic resistance");
                        state.sendBasicResistance();
                        break;
                    case TrackResistance.PAGE_NUMBER:
                        logger.trace("requested track resistance");
                        state.sendTrackResistance();
                        break;
                    case WindResistance.PAGE_NUMBER:
                        logger.trace("requested wind data");
                        state.sendWindData();
                        break;
                }
            }
        });

        pageDispatcher.addListener(ConfigPage.class, new BroadcastListener<ConfigPage>() {

            @Override
            public void receiveMessage(ConfigPage page) {
                state.useConfig(page.getConfig());
            }
        });

        pageDispatcher.addListener(PercentageResistance.class, new BroadcastListener<PercentageResistance>() {
            @Override
            public void receiveMessage(PercentageResistance percentageResistance) {
                state.setBasicResistance(percentageResistance);
            }
        });

        pageDispatcher.addListener(TrackResistance.class, new BroadcastListener<TrackResistance>() {
            @Override
            public void receiveMessage(TrackResistance page) {
                state.setTrackResistance(page);
            }
        });

        pageDispatcher.addListener(WindResistance.class, new BroadcastListener<WindResistance>() {
            @Override
            public void receiveMessage(WindResistance page) {
                state.setWindResistance(page);
            }
        });

        pageDispatcher.addListener(CalibrationResponse.class, new BroadcastListener<CalibrationResponse>() {
            @Override
            public void receiveMessage(CalibrationResponse calibrationResponse) {
                if (calibrationResponse.isSpinDownSuccess()) {
                    state.requestSpinDownCalibration();
                }
                if (calibrationResponse.isZeroOffsetSuccess()) {
                    state.requestOffsetCalibration();
                }
            }
        });



        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                state.incrementLaps();

            }
        }, 1000, 60000);


        final Channel channel = transceiver.getFreeChannel();
        ChannelType type = new MasterChannelType(false, false);
        channel.assign(NetworkKeys.ANT_SPORT, type);
        channel.setId(1234,17,255,false);
        channel.setPeriod(8192);
        channel.setFrequency(57);
        channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);
        channel.registerRxListener(new BroadcastListener<DataMessage>() {
            @Override
            public void receiveMessage(DataMessage msg) {
                byte [] data = msg.getPrimitiveData();

                pageDispatcher.dispatch(data);
                CommandId cmd = CommandId.getValueFromInt(getPageNum(data));
                if (cmd != CommandId.UNRECOGNIZED && cmd != CommandId.NO_CONTROL_PAGE_RECEIVED) {
                    logger.trace("receieved cmd: " + cmd);
                }
                logger.trace(Format.bytesToString(msg.getData()));
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

    @Override
    public void setPower(int power) {
        state.setPower(power);
    }

    @Override
    public void setHeartrate(int hr) {
        state.setHeartRate(hr);
    }

    @Override
    public void incrementLaps() {
        // we could buffer these requests, but I don't think there is a
        // pressing need for such short laps
        try {
            state.incrementLaps();
        } catch (IllegalStateException e) {
            logger.warn("increment laps ignore, polling to fast!");
        }
    }

    @Override
    public TurboStateViewable getState() {
        return state;
    }
}
