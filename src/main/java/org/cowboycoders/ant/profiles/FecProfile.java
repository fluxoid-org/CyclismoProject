package org.cowboycoders.ant.profiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cowboycoders.ant.Channel;
import org.cowboycoders.ant.ChannelEventHandler;
import org.cowboycoders.ant.ChannelId;
import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.SlaveChannelType;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;
import org.cowboycoders.ant.messages.data.DataMessage;
import org.cowboycoders.ant.profiles.common.PageDispatcher;
import org.cowboycoders.ant.profiles.fitnessequipment.Capabilities;
import org.cowboycoders.ant.profiles.fitnessequipment.Config;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.*;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.cowboycoders.ant.profiles.pages.Request;
import org.cowboycoders.ant.profiles.simulators.NetworkKeys;

import java.math.BigDecimal;

import static org.cowboycoders.ant.profiles.common.PageDispatcher.getPageNum;
import static org.fluxoid.utils.Format.bytesToString;

/**
 * Created by fluxoid on 02/07/17.
 */
public abstract class FecProfile {

    private Channel channel;

    // for BikeData, CommonPageData
    private boolean lapFlag;


    public static void main(String [] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        node.start();
        node.reset();
        new FecProfile() {
            @Override
            public void onCapabilitiesReceived(Capabilities capabilitiesPage) {

            }

            @Override
            public void onConfigRecieved(Config conf) {

            }

        }.start(node);
    }


    private Logger logger = LogManager.getLogger();

    public void requestCapabilities() {
        requestPage(CapabilitiesPage.PAGE_NUMBER);
    }

    public void requestConfig() {
        requestPage(ConfigPage.PAGE_NUMBER);
    }

    public void requestStatusCmd() {
        requestPage(Command.PAGE_NUMBER);
    }

    public void requestStatusCalibration() {
        requestPage(CalibrationResponse.PAGE_NUMBER);
    }

    public void requestStatusBasic() {
        requestPage(PercentageResistance.PAGE_NUMBER);
    }

    public void requestStatusTrack() {
        requestPage(TrackResistance.PAGE_NUMBER);
    }

    public void requestStatusWind() {
        requestPage(WindResistance.PAGE_NUMBER);
    }

    public void setConfig(Config config) {
        sendEncodable(
        new ConfigPage.ConfigPayload().
                setConfig(config)
        );
    }

    public void setBasicResistance(double gradient) {
        sendEncodable(
        new PercentageResistance.PercentageResistancePayload()
            .setResistance(new BigDecimal(gradient)));
    }

    public void setWindResistance(WindResistance.WindResistancePayload settings) {
        sendEncodable(settings);
    }

    public void setTrackResistance(TrackResistance.TrackResistancePayload settings) {
        sendEncodable(settings);
    }

    public void requestSpinDownCalibration() {
        sendEncodable(new CalibrationResponse.CalibrationResponsePayload()
        .setSpinDownSuccess(true));
    }

    public void requestZeroOffsetCalibration() {
        sendEncodable(
                new CalibrationResponse.CalibrationResponsePayload()
                .setZeroOffsetSuccess(true)
        );
    }


    public void incrementLaps() {
        lapFlag = !lapFlag;
    }



    private void sendEncodable(AntPacketEncodable encodable) {
        byte [] data = new byte[8];
        encodable.encode(data);
        BroadcastDataMessage payload = new BroadcastDataMessage();
        payload.setData(data);
        channel.send(payload);
    }



    private void requestPage(int pageNumber) {
        sendEncodable(
        new Request.RequestPayload()
                .setPageNumber(pageNumber)
        );

    }


    public void start(Node transceiver) {

        final PageDispatcher pageDispatcher = new PageDispatcher();


        pageDispatcher.addListener(AntPage.class, new BroadcastListener<AntPage>() {

            @Override
            public void receiveMessage(AntPage page) {
                final int pageNum = page.getPageNumber();
                switch (pageNum) {
                    case CalibrationProgress.PAGE_NUMBER:
                    case TrainerData.PAGE_NUMBER:
                    case TorqueData.PAGE_NUMBER:
                    case GeneralData.PAGE_NUMBER:
                    case BikeData.PAGE_NUMBER:
                    case MetabolicData.PAGE_NUMBER:
                    case CapabilitiesPage.PAGE_NUMBER:
                        return; // don't print pages we already handle
                }
                System.out.print("got page: " + page);

            }
        });

        pageDispatcher.addListener(ConfigPage.class, new BroadcastListener<ConfigPage>() {
            @Override
            public void receiveMessage(ConfigPage configPage) {
                FecProfile.this.onConfigRecieved(configPage.getConfig());
            }
        });

        pageDispatcher.addListener(CapabilitiesPage.class, new BroadcastListener<CapabilitiesPage>() {
            @Override
            public void receiveMessage(CapabilitiesPage capabilitiesPage) {
                FecProfile.this.onCapabilitiesReceived(capabilitiesPage.getCapabilites());
            }
        });

        pageDispatcher.addListener(CalibrationProgress.class, new BroadcastListener<CalibrationProgress>() {
            @Override
            public void receiveMessage(CalibrationProgress calibrationProgress) {
                // TODO: forward on to client
            }
        });

        pageDispatcher.addListener(TrainerData.class, new BroadcastListener<TrainerData>() {
            @Override
            public void receiveMessage(TrainerData trainerData) {
                // TODO: decode trainerdata
            }
        });

        pageDispatcher.addListener(TorqueData.class, new BroadcastListener<TorqueData>() {
            @Override
            public void receiveMessage(TorqueData torqueData) {
                // TODO: decode torqueData
            }
        });

        pageDispatcher.addListener(GeneralData.class, new BroadcastListener<GeneralData>() {
            @Override
            public void receiveMessage(GeneralData generalData) {
                // TODO: decode general
            }
        });

        pageDispatcher.addListener(BikeData.class, new BroadcastListener<BikeData>() {
            @Override
            public void receiveMessage(BikeData bikeData) {
                // TODO: decode bike data
            }
        });

        pageDispatcher.addListener(MetabolicData.class, new BroadcastListener<MetabolicData>() {
            @Override
            public void receiveMessage(MetabolicData metabolicData) {
                // TODO: decode metabolic
            }
        });


        channel = transceiver.getFreeChannel();
        ChannelType type = new SlaveChannelType(false, false);
        channel.assign(NetworkKeys.ANT_SPORT, type);
        channel.setId(ChannelId.WILDCARD,17,ChannelId.WILDCARD,false);
        channel.setPeriod(8192);
        channel.setFrequency(57);
        channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);
        channel.registerRxListener(new BroadcastListener<DataMessage>() {
            @Override
            public void receiveMessage(DataMessage msg) {
                byte [] data = msg.getPrimitiveData();

                pageDispatcher.dispatch(data);
                Defines.CommandId cmd = Defines.CommandId.getValueFromInt(getPageNum(data));
                if (cmd != Defines.CommandId.UNRECOGNIZED && cmd != Defines.CommandId.NO_CONTROL_PAGE_RECEIVED) {
                    logger.trace("receieved cmd: " + cmd);
                }
                logger.trace(bytesToString(msg.getData()));
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

            }
        });

        channel.open();

    }

    public abstract void onCapabilitiesReceived(Capabilities capabilitiesPage);
    public abstract void onConfigRecieved(Config conf);
}
