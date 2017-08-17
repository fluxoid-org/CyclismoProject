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
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.PageDispatcher;
import org.cowboycoders.ant.profiles.common.decode.*;
import org.cowboycoders.ant.profiles.common.events.*;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
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
import static org.cowboycoders.ant.profiles.common.utils.PayloadUtils.getBroadcastDataMessage;
import static org.fluxoid.utils.Format.bytesToString;

/**
 * Created by fluxoid on 02/07/17.
 */
public abstract class FecProfile {

    private static final BigDecimal WHEEL_CIRCUMFERENCE = new BigDecimal(2.098);
    private Channel channel;

    // for BikeData, CommonPageData
    private Defines.EquipmentType equipType = Defines.EquipmentType.UNRECOGNIZED;
    private BroadcastListener<Defines.EquipmentType> typeCallBack = null;
    private Defines.EquipmentState state = Defines.EquipmentState.UNRECOGNIZED;


    public static void main(String [] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        node.start();
        node.reset();
        new FecProfile() {
            @Override
            public void onEquipmentStateChange(Defines.EquipmentState oldState, Defines.EquipmentState newState) {

            }

            @Override
            public void onCapabilitiesReceived(Capabilities capabilitiesPage) {

            }

            @Override
            public void onConfigRecieved(Config conf) {

            }

            @Override
            public void onCalibrationUpdate(CalibrationProgress progress) {

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
        updateDecoders(config);
    }

    /**
     * Should update decoders to use new values from config
     * @param config to obtain data from
     */
    private void updateDecoders(Config config) {
        BigDecimal actualCircum = config.getBicycleWheelDiameter().multiply(
                new BigDecimal(Math.PI));
        rotToDistDecoder = new RotationsToDistanceDecoder<>(dataHub, actualCircum);
        speedDecoder = new SpeedDecoder<>(dataHub, actualCircum);
    }

    /**
     *
     * @param value either percentage (0.5% resolution) or an absolute value 0-255 depending on the model
     */
    public void setBasicResistance(double value) {
        sendEncodable(
        new PercentageResistance.PercentageResistancePayload()
            .setResistance(new BigDecimal(value)));
    }

    /**
     * @param power range 0-1000w, resolution: 0.25
     */
    public void setTargetPower(double power) {
        sendEncodable(
                new TargetPower.TargetPowerPayload()
                .setTargetPower(new BigDecimal(power)
        ));
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


    private void sendEncodable(AntPacketEncodable encodable) {
        BroadcastDataMessage payload = getBroadcastDataMessage(encodable);
        channel.send(payload);
    }


    private void requestPage(int pageNumber) {
        sendEncodable(
        new Request.RequestPayload()
                .setPageNumber(pageNumber)
        );

    }



    private final FilteredBroadcastMessenger<TaggedTelemetryEvent> dataHub = new FilteredBroadcastMessenger<>();

    public FilteredBroadcastMessenger<TaggedTelemetryEvent> getDataHub() {
        return dataHub;
    }

    final LapFlagDecoder<CommonPageData> lapDecoder = new LapFlagDecoder<>(dataHub);

    private void handleCommon(CommonPageData data) {
        setState(data);
        lapDecoder.update(data);
    }

    private void setState(CommonPageData generalData) {
        if (state != generalData.getState()) {
            Defines.EquipmentState newState = generalData.getState();
            onEquipmentStateChange(state, newState);
            state = newState;
        }
    }

    // initialise with approx wheel diameter, will use value from config when it is determined
    private RotationsToDistanceDecoder<TorqueData> rotToDistDecoder = new RotationsToDistanceDecoder<>(dataHub,
            WHEEL_CIRCUMFERENCE);

    private SpeedDecoder<TorqueData> speedDecoder = new SpeedDecoder<>(dataHub, WHEEL_CIRCUMFERENCE);

    public void start(Node transceiver) {

        final PageDispatcher pageDispatcher = new PageDispatcher();

        final PowerOnlyDecoder<TrainerData> trainerDataDecoder = new PowerOnlyDecoder<>(dataHub);
        final AccDistanceDecoder<GeneralData> accDistDecoder = new AccDistanceDecoder<>(dataHub);
        final TimeDecoder<GeneralData> timeDecoder = new TimeDecoder<>(dataHub);
        final CalorieCountDecoder<MetabolicData> calorieDecoder = new CalorieCountDecoder<>(dataHub);

        final TorqueDecoder<TorqueData> torqueDecoder = new TorqueDecoder<>(dataHub);

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
                    case ConfigPage.PAGE_NUMBER:
                        return; // don't print pages we already handle
                }
                System.out.print("got page: " + page);

            }
        });

        pageDispatcher.addListener(ConfigPage.class, new BroadcastListener<ConfigPage>() {
            @Override
            public void receiveMessage(ConfigPage configPage) {
                FecProfile.this.onConfigRecieved(configPage.getConfig());
                updateDecoders(configPage.getConfig());

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
                onCalibrationUpdate(calibrationProgress);
            }
        });

        pageDispatcher.addListener(TrainerData.class, new BroadcastListener<TrainerData>() {
            @Override
            public void receiveMessage(TrainerData trainerData) {
                trainerDataDecoder.update(trainerData);
            }
        });

        pageDispatcher.addListener(TorqueData.class, new BroadcastListener<TorqueData>() {
            @Override
            public void receiveMessage(TorqueData torqueData) {
                torqueDecoder.update(torqueData);
                handleCommon(torqueData);
                rotToDistDecoder.update(torqueData);
                speedDecoder.update(torqueData);
            }
        });


        pageDispatcher.addListener(GeneralData.class, new BroadcastListener<GeneralData>() {


            @Override
            public void receiveMessage(GeneralData generalData) {
                accDistDecoder.update(generalData);
                timeDecoder.update(generalData);
                dataHub.send(new HeartRateUpdate(generalData.getClass(), generalData.getHeartRateSource(), generalData.getHeartRate()));
                dataHub.send(new SpeedUpdate(generalData.getClass(), generalData.getSpeed(), generalData.isUsingVirtualSpeed()));
                setEquipmentType(generalData.getType());
                handleCommon(generalData);
            }

        });

        pageDispatcher.addListener(BikeData.class, new BroadcastListener<BikeData>() {
            @Override
            public void receiveMessage(BikeData bikeData) {
                dataHub.send(new InstantPowerUpdate(BikeData.class, new BigDecimal(bikeData.getPower())));
                dataHub.send(new CadenceUpdate(BikeData.class, bikeData.getCadence()));
                handleCommon(bikeData);
            }
        });

        pageDispatcher.addListener(MetabolicData.class, new BroadcastListener<MetabolicData>() {
            @Override
            public void receiveMessage(MetabolicData metabolicData) {
                calorieDecoder.update(metabolicData);
                handleCommon(metabolicData);
                dataHub.send(new InstantMetabolicUpdate(MetabolicData.class,
                        metabolicData.getInstantCalorieBurn(),
                        metabolicData.getInstantMetabolicEquivalent()));

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


        // request config : we are assuming the wheel diameter is stored in here
        channel.registerRxListener(new BroadcastListener<DataMessage>() {
            @Override
            public void receiveMessage(DataMessage channelMessage) {
                requestConfig();
                channel.removeRxListener(this);
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


    /**
     *
     * @param callback will be called once, setting multiple times will overwrite last
     */
    public void getEquipmentType(BroadcastListener<Defines.EquipmentType> callback) {
        if (equipType != Defines.EquipmentType.UNRECOGNIZED) {
            callback.receiveMessage(equipType);
        }
        typeCallBack = callback;
    }

    private void setEquipmentType(Defines.EquipmentType type) {
        equipType = type;
        if (typeCallBack != null) {
            typeCallBack.receiveMessage(type);
            typeCallBack = null; // only call once
        }
    }

    public abstract void onEquipmentStateChange(Defines.EquipmentState oldState, Defines.EquipmentState newState);
    public abstract void onCapabilitiesReceived(Capabilities capabilitiesPage);
    public abstract void onConfigRecieved(Config conf);
    public abstract void onCalibrationUpdate(CalibrationProgress progress);
}
