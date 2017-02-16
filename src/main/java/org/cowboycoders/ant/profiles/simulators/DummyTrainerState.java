package org.cowboycoders.ant.profiles.simulators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cowboycoders.ant.profiles.fitnessequipment.*;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines.CommandId;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines.SpeedCondition;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.*;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.GeneralData.GeneralDataPayload;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.CommonCommandPage;
import org.fluxoid.utils.MathCompat;
import org.fluxoid.utils.RollOverVal;
import org.fluxoid.utils.RotatingView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static java.lang.Math.PI;

/**
 * Created by fluxoid on 01/02/17.
 */
public class DummyTrainerState {


    public static final double CALORIES_TO_JOULES = 4.2;
    public static final double HUMAN_EFFICENCY_FUDGE_FACTOR = 1 / 0.3;
    public static final double WATTS_TO_KJ_PER_HOUR = 3.6;
    private Logger logger = LogManager.getLogger();

    // 700c http://www.bikecalc.com/wheel_size_math
    private BigDecimal wheelDiameter = new BigDecimal(0.700).divide(new BigDecimal(PI),4, RoundingMode.HALF_UP);
    private BigDecimal resistance = new BigDecimal(0);
    private BigDecimal internalTemp = new BigDecimal(66.6);

    private boolean calibrationInProgress = false;

    private BigDecimal getWheelCircumference() {
        return wheelDiameter.multiply(new BigDecimal(PI));
    }
    // for intense cycling!
    // estimated from https://en.wikipedia.org/wiki/Metabolic_equivalent
    public static final double MET_CYCLING = 7.5;
    public static final int ATHLETE_HEIGHT = 180;
    public static final int ATHLETE_AGE = 21;

    private boolean lapFlag;
    private int power;
    private int cadence;
    private BigDecimal speed = new BigDecimal(0.0);
    private Defines.EquipmentState state = Defines.EquipmentState.READY;
    private static final Defines.EquipmentType type = Defines.EquipmentType.TRAINER;

    private Athlete athlete = new MaleAthlete(ATHLETE_HEIGHT, 80, ATHLETE_AGE);
    private BigDecimal bikeWeight = new BigDecimal(10); // kg
    private BigDecimal gearRatio = getGearRatio(52, 11);
    private Capabilities capabilities = new CapabilitiesBuilder()
            .setBasicResistanceModeSupport(true)
            .setSimulationModeSupport(false)
            .setTargetPowerModeSupport(false)
            .setMaximumResistance(1234)
            .createCapabilities();

    private RollOverVal seqNum = new RollOverVal(255);
    private CommandId lastCmd = CommandId.UNRECOGNIZED;

    private List<PageGen> priorityMessages = new LinkedList<>();

    /**
     * Front to back ratio
     * @param inputTeeth numbre of teeth on chainring
     * @param outputTeeth number of teeth on cassette sprocket
     * @return
     */
    private static BigDecimal getGearRatio(int inputTeeth, int outputTeeth) {
        return new BigDecimal(inputTeeth).divide(new BigDecimal(outputTeeth), 4 ,RoundingMode.HALF_UP);
    }

    private Config getConfig() {
        return new ConfigBuilder()
                .setBicycleWeight(bikeWeight)
                .setBicycleWheelDiameter(wheelDiameter)
                .setGearRatio(gearRatio)
                .setUserWeight(new BigDecimal(athlete.getWeight()))
                .createConfig();
    }

    // cleared when common page data is generated
    private boolean lapFlagIsDirty = false;
    private long powerEvents;
    private long powerSum;

    private <V extends CommonPageData.CommonPagePayload> V setCommon(V payload) {
        lapFlagIsDirty = false;
        payload.setLapFlag(lapFlag)
                .setState(state);
        return payload;
    }

    private int distance = 0;
    private Integer heartRate;

    private long start;

    public DummyTrainerState() {
        start = System.nanoTime();
    }

    public DummyTrainerState setPower(int power) {
        this.power = power;
        return this;
    }

    public DummyTrainerState setCadence(int cadence) {
        this.cadence = cadence;
        return this;
    }

    public DummyTrainerState setSpeed(BigDecimal speed) {
        this.speed = speed;
        return this;
    }


    public DummyTrainerState setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
        return this;
    }

    public int getDistance() {
        return distance;
    }

    /**
     * the assumption is that this is called infrequently, so we
     * get a change to transmit the old flag;
     */
    public void incrementLaps() {
        if (lapFlagIsDirty) {
            throw new IllegalStateException("you are polling incrementLaps too quickly");
        }
        lapFlag =! lapFlag;
        lapFlagIsDirty = true;
    }

    private PageGen basicCmdStatusGen = new PageGen() {
        @Override
        public AntPacketEncodable getPageEncoder() {
            CommonCommandPage.CommandStatus status = new CommonCommandPage.CommandStatusBuilder()
                    .setLastReceivedSequenceNumber(MathCompat.toIntExact(seqNum.get()))
                    .setStatus(Defines.Status.PASS) // we can instantly set resistance
                    .createCommandStatus();
            return new Command.ResistanceStatusBuilder()
                    .setStatus(status)
                    .setTotalResistance(resistance);
        }
    };

    private PageGen generalDataGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            long now =  System.nanoTime();
            BigDecimal elapsed = new BigDecimal((now - start) / Math.pow(10,9));
            return setCommon(
                    new GeneralDataPayload()
                    .setType(type)
                    .setHeartRate(heartRate)
                    .setHeartRateSource(Defines.HeartRateDataSource.ANTPLUS_HRM)
                    .setDistanceCovered(distance)
                    .setTimeElapsed(elapsed)
                    .setUsingVirtualSpeed(false)
                    .setSpeed(speed)

            );
        }
    };

    private PageGen bikeDataGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            return setCommon(
                    new BikeData.BikeDataPayload()
                            .setPower(power)
                            .setCadence(cadence)
            );
        }
    };

    private EnumSet<Defines.TrainerStatusFlag> statusFlags = EnumSet.noneOf(Defines.TrainerStatusFlag.class);

    public EnumSet<Defines.TrainerStatusFlag> getStatusFlags() {
        return statusFlags;
    }

    public DummyTrainerState setStatusFlags(EnumSet<Defines.TrainerStatusFlag> statusFlags) {
        this.statusFlags = statusFlags;
        return this;
    }

    // not used by EquipmentType.Bike
    private PageGen trainerDataGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            powerEvents += 1;
            powerSum += power;
            return setCommon(
                    new TrainerData.TrainerDataPayload()
                            .setInstantPower(power)
                    .setCadence(cadence)
                    .setPowerSum(powerSum)
                    .setEvents(MathCompat.toIntExact(powerEvents))
                    .setTrainerStatusFlags(statusFlags)
                            
            );
        }
    };

    private int torqueEvents;
    private long torqueTimeStamp = System.nanoTime();

    private final TorqueData.TorqueDataPayload torqueDataPayload = new TorqueData.TorqueDataPayload();
    private PageGen torqueDataGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            torqueEvents += 1;
            if (speed.compareTo(new BigDecimal(0.1)) < 0) {
                // assume stationary to stop divide by zero when calculating period i.e period -> infinity
                return setCommon(
                        torqueDataPayload
                                .setEvents(torqueEvents)
                );
            }

            // period for 1 rotation
            BigDecimal period = getWheelCircumference().divide(speed, 20, BigDecimal.ROUND_HALF_UP);

            long now = System.nanoTime();
            double delta = (now - torqueTimeStamp) / Math.pow(10,9);
            BigDecimal rotations = new BigDecimal(delta).divide(period, 0, BigDecimal.ROUND_HALF_UP);
            return setCommon(
                    torqueDataPayload
                    .setEvents(torqueEvents)
                    .updateTorqueSumFromPower(power, period)
                    .setRotations(rotations.longValue())
            );
        }
    };

    private BigDecimal getInstantCalorieBurn() {
        double kcal = (HUMAN_EFFICENCY_FUDGE_FACTOR * ((power * WATTS_TO_KJ_PER_HOUR) / CALORIES_TO_JOULES));
        return new BigDecimal(kcal);
    }

    private BigDecimal getApproxMet() {
        return getInstantCalorieBurn().divide(athlete.getBrmPerHour(), 2, RoundingMode.HALF_UP);
    }


    private PageGen metabolicGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            long timeNanos = System.nanoTime() - start;
            double timeHrs = timeNanos / Math.pow(10, 9) / (60 * 60);
            BigDecimal instantCalorieBurn = getInstantCalorieBurn();
            BigDecimal calsBurnt = instantCalorieBurn.multiply(new BigDecimal(timeHrs));
            return setCommon(
                    new MetabolicData.MetabolicDataPayload()
                            .setInstantCalorieBurn(instantCalorieBurn)
                            .setCalorieCounter(calsBurnt.intValue())
                            .setInstantMetabolicEquivalents(getApproxMet())

            );
        }
    };

    private PageGen configGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            return new ConfigPage.ConfigPayload()
                    .setConfig(getConfig());

        }
    };

    private PageGen capabilitiesGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            return new CapabilitiesPage.CapabilitiesPayload()
                    .setCapabilites(capabilities);

        }
    };


    private PageGen [] normalPages = new PageGen[] {generalDataGen, bikeDataGen, metabolicGen, torqueDataGen};

    // type = BIKE doesn't use capabilities, torqueData
    //private RotatingView<PageGen> packetGen = new RotatingView<> (normalPages);

    private static interface OperationMode {

        public OperationMode update();

        RotatingView<PageGen> getPacketGenerators();
    }

    private OperationMode normalMode = new OperationMode() {

        private RotatingView<PageGen> normalPackets = new RotatingView<>(normalPages);

        @Override
        public OperationMode update() {
            return this;
        }

        @Override
        public RotatingView<PageGen> getPacketGenerators() {
            return normalPackets;
        }
    };

    private enum SpinDownCalibrationState {
        AWAITING_SPEED_UP,
        REACHED_SPEED,
        AWAITING_SPEED_DOWN,
    }

    private static final BigDecimal TARGET_SPEED = new BigDecimal(10);
    // speed in which it assumed that the bike is stationary; this is necessary since some turbo trainers are unpowered
    // and cannot transmit packets when the speed is below a certain threshold
    private static final BigDecimal ZERO_SPEED_THRESHOLD = new BigDecimal(1.6);

    private PageGen calibrationResponseGen = new PageGen() {
        @Override
        public AntPacketEncodable getPageEncoder() {
            return new CalibrationResponse.CalibrationResponsePayload()
                    .setSpinDownSuccess(true)
                    .setSpinDownTime(10000)
                    .setTemp(internalTemp);
        }
    };
    private OperationMode spinDownCalibrationMode = new OperationMode() {

        private SpinDownCalibrationState state = SpinDownCalibrationState.AWAITING_SPEED_UP;

        private final PageGen calibrationStatusGen = new PageGen() {
            @Override
            public AntPacketEncodable getPageEncoder() {
                return new CalibrationProgress.CalibrationProgressPayload()
                        .setTemp(internalTemp)
                        .setTempState(Defines.TemperatureCondition.CURRENT_TEMPERATURE_OK)
                        .setSpeedState(getSpeedState())
                        .setSpinDownPending(true)
                        .setTargetSpeed(TARGET_SPEED)
                        .setTargetSpinDownTime(10000);
            }

        };

        private PageGen [] calibrationPages;
        {
            // this needs normalPages to be defined before this in the source file
            List<PageGen> t = new ArrayList<>(Arrays.asList(normalPages));
            t.add(calibrationStatusGen);
            calibrationPages = t.toArray(new PageGen[0]);

        };

        private SpeedCondition getSpeedState() {
            switch (state) {
                case AWAITING_SPEED_UP: return SpeedCondition.CURRENT_SPEED_TOO_LOW;
                default: return SpeedCondition.CURRENT_SPEED_OK;
            }
        }

        private RotatingView<PageGen> calibrationGen = new RotatingView<>(calibrationPages);

        private Long start;

        private void reset() {
            start = null;
            state = SpinDownCalibrationState.AWAITING_SPEED_UP;
        }

        @Override
        public OperationMode update() {
            if (start == null) {
                start = System.nanoTime();
            }
            switch (state) {
                case AWAITING_SPEED_UP:
                    if (speed.compareTo(TARGET_SPEED) >= 0) {
                        state = SpinDownCalibrationState.REACHED_SPEED;
                    }
                    return this;
                case AWAITING_SPEED_DOWN:
                    if (speed.compareTo(ZERO_SPEED_THRESHOLD) <= 0) {
                        state = SpinDownCalibrationState.REACHED_SPEED;
                    }
                    return this;
                case REACHED_SPEED:
                    if (speed.compareTo(ZERO_SPEED_THRESHOLD) <= 0) {
                        // finish condition
                        long delta = System.nanoTime() - start;
                        final BigDecimal spinDownTimeMillis = new BigDecimal(delta)
                                .divide(new BigDecimal(Math.pow(10, 6)), 0, RoundingMode.HALF_UP);
                        logger.trace("spindown calibration time (ms): {}", spinDownTimeMillis );
                        priorityMessages.add(new PageGen() {
                            @Override
                            public AntPacketEncodable getPageEncoder() {
                                return new CalibrationResponse.CalibrationResponsePayload()
                                        .setSpinDownSuccess(true)
                                        .setSpinDownTime(spinDownTimeMillis.intValue())
                                        .setTemp(internalTemp);
                            }
                        });
                        reset();
                        return normalMode;
                    }
                    state = SpinDownCalibrationState.AWAITING_SPEED_DOWN;
                    return this;


            }
            return this;
        }

        @Override
        public RotatingView<PageGen> getPacketGenerators() {
            return calibrationGen;
        }
    };

    private OperationMode mode = normalMode;


    public void setCapabilitesRequested() {
        priorityMessages.add(capabilitiesGen);
    }

    public void setConfigRequested() {
        priorityMessages.add(configGen);
    }

    private BigDecimal getTimeSinceStart() {
        long now = System.nanoTime();
        return new BigDecimal(now - start)
                .divide(new BigDecimal(Math.pow(10, 9)),10, RoundingMode.HALF_UP);
    }

    public byte [] nextPacket() {
        mode = mode.update();
        distance = getTimeSinceStart().multiply(speed).intValue();
        final byte [] packet = new byte[8];

        if (priorityMessages.isEmpty()) {
            mode.getPacketGenerators().rotate().getPageEncoder().encode(packet);
        } else {
            priorityMessages.remove(0).getPageEncoder().encode(packet);
        }

        return packet;
    }

    private void onCmdReceieved(CommandId commandId) {
        lastCmd = commandId;
        seqNum.add(1);
    }

    public void setResistance(BigDecimal resisitance) {
        onCmdReceieved(CommandId.BASIC_RESISTANCE);
        this.resistance = resisitance;
    }

    public void useConfig(Config config) {
        // we don't get sent sex, height,
        athlete = new MaleAthlete(ATHLETE_HEIGHT, config.getUserWeight().doubleValue(), ATHLETE_HEIGHT);
        bikeWeight = config.getBicycleWeight();
        gearRatio = config.getGearRatio();
        wheelDiameter = config.getBicycleWheelDiameter();
    }

    public void sendCmdStatus() {
        switch (lastCmd) {
            case BASIC_RESISTANCE:
                priorityMessages.add(basicCmdStatusGen);
                break;
        }

    }

    public void requestSpinDownCalibration() {
        mode = spinDownCalibrationMode;
    }

    public void requestOffsetCalibration() {
        logger.warn("offset calibration requested, but emulating this is not supported yet");
    }

    public void sendCalibrationResponse() {
        priorityMessages.add(calibrationResponseGen);
    }


}
