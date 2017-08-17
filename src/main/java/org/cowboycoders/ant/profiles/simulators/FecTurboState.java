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
import org.cowboycoders.turbotrainers.PowerModel;
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
public class FecTurboState implements TurboStateViewable {


    public static final double CALORIES_TO_JOULES = 4.2;
    public static final double HUMAN_EFFICENCY_FUDGE_FACTOR = 1 / 0.3;
    public static final double WATTS_TO_KJ_PER_HOUR = 3.6;
    public static final int MAX_GRADIENT_BASIC_RESISTANCE = 30;
    private Logger logger = LogManager.getLogger();

    // 700c http://www.bikecalc.com/wheel_size_math
    private BigDecimal wheelDiameter = new BigDecimal(0.700).divide(new BigDecimal(PI),4, RoundingMode.HALF_UP);
    private BigDecimal resistance = new BigDecimal(0);
    private BigDecimal internalTemp = new BigDecimal(66.6);


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
    private PowerModel powerModel = new PowerModel() {
        @Override
        public double getPowerLostToAerodynamicDrag() {

            // TODO: think about how this works for a tail wind
            double Vg = Math.abs(this.getVelocity());

            //https://en.wikipedia.org/wiki/Drag_equation
            BigDecimal preScaled = new BigDecimal(0.5)
                    .multiply(windResistance.getWindResistanceCoefficent())
                    .multiply(new BigDecimal(this.getAirVelocity()).pow(2))
                    .multiply(new BigDecimal(Vg));


            return windResistance.getDraftingFactor()
                    .multiply(preScaled)
                    .doubleValue();
        }
    };


    @Override
    public int getPower() {
        return power;
    }

    @Override
    public int getCadence() {
        return cadence;
    }

    @Override
    public BigDecimal getSpeed() {
        return toKmh(speed);
    }

    @Override
    public Athlete getAthlete() {
        return athlete;
    }

    @Override
    public BigDecimal getBikeWeight() {
        return bikeWeight;
    }

    @Override
    public BigDecimal getGearRatio() {
        return gearRatio;
    }

    @Override
    public OperationState getState() {
        return mode;
    }

    @Override
    public BigDecimal getWheelDiameter() {
        return wheelDiameter;
    }

    @Override
    public TrackResistance getTrackResistance() {
        return trackResistance;
    }

    @Override
    public WindResistance getWindResistance() {
        return windResistance;
    }

    @Override
    public Integer getHeartRate() {
        return heartRate;
    }

    public void setTrackResistance(TrackResistance trackResistance) {
        onCmdReceieved(CommandId.TRACK_RESISTANCE);
        this.trackResistance = trackResistance;
    }

    public void setWindResistance(WindResistance windResistance) {
        onCmdReceieved(CommandId.WIND_RESISTANCE);
        this.windResistance = windResistance;
    }

    public void setBasicResistance(PercentageResistance resistance) {
        onCmdReceieved(CommandId.BASIC_RESISTANCE);
        this.resistance = resistance.getResistance();
        // basic resistance is simulated by just adjusting the gradient
        this.windResistance = new WindResistance.WindResistancePayload().createWindResistance();
        BigDecimal validated = resistance.getResistance().min(new BigDecimal(100.0)).max(new BigDecimal(0))
                .divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
        final BigDecimal calculatedGradient = validated.multiply(new BigDecimal(MAX_GRADIENT_BASIC_RESISTANCE));
        logger.trace("setting calculated gradient: {}",calculatedGradient);
        this.trackResistance = new TrackResistance.TrackResistancePayload()
                .setGradient(calculatedGradient)
                .createTrackResistance();
    }

    public void setTargetPower(TargetPower packet) {
        onCmdReceieved(CommandId.TARGET_POWER);
        setPower((packet.getTargetPower().intValue()));
    }

    private Athlete athlete = new MaleAthlete(ATHLETE_HEIGHT, 80, ATHLETE_AGE);
    private BigDecimal bikeWeight = new BigDecimal(10); // kg
    private BigDecimal gearRatio = getGearRatio(52, 11);
    private Capabilities capabilities = new CapabilitiesBuilder()
            .setBasicResistanceModeSupport(true)
            .setSimulationModeSupport(true)
            .setTargetPowerModeSupport(true)
            .setMaximumResistance(1234)
            .createCapabilities();

    private RollOverVal seqNum = new RollOverVal(255);
    private CommandId lastCmd = CommandId.UNRECOGNIZED;

    private List<PageGen> priorityMessages = new LinkedList<>();

    private TrackResistance trackResistance = new TrackResistance.TrackResistancePayload().createTrackResistance();
    private WindResistance windResistance = new WindResistance.WindResistancePayload().createWindResistance();

    /**
     * Front to back ratio
     * @param inputTeeth number of teeth on chain-ring
     * @param outputTeeth number of teeth on cassette sprocket
     * @return computed ratio
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

    private long distance = 0;
    private Integer heartRate;

    private long start;

    public FecTurboState() {
        start = System.nanoTime();
    }

    public FecTurboState setPower(int power) {
        this.power = power;
        return this;
    }

    public FecTurboState setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
        return this;
    }

    @Override
    public long getDistance() {
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



    private PageGen cmdStatusGen = new PageGen() {
        @Override
        public AntPacketEncodable getPageEncoder() {
            CommonCommandPage.CommandStatus status = new CommonCommandPage.CommandStatusBuilder()
                    .setLastReceivedSequenceNumber(MathCompat.toIntExact(seqNum.get()))
                    .setStatus(Defines.Status.PASS) // we can instantly set resistance
                    .createCommandStatus();
            // we should probably send the data we actually last received
            // currently setting a basic resistance will clobber the old
            // values
            switch (lastCmd) {
                case BASIC_RESISTANCE: return new Command.ResistanceStatusBuilder()
                        .setStatus(status)
                        .setTotalResistance(resistance);
                case TRACK_RESISTANCE: return new Command.TerrainStatusBuilder()
                        .setStatus(status)
                        .setGrade(trackResistance.getGradient())
                        .setRollingResistanceCoefficient(trackResistance.getGradient());
                case WIND_RESISTANCE: return new Command.WindStatusBuilder()
                        .setStatus(status)
                        .setDraftingFactor(windResistance.getDraftingFactor())
                        .setWindResistanceCoefficient(windResistance.getWindResistanceCoefficent())
                        .setWindSpeed(windResistance.getWindSpeed());

            }
            logger.warn("unsupported command: {}", lastCmd);
            return null;
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
                    .setSpeed(getSpeed())

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

    public FecTurboState setStatusFlags(EnumSet<Defines.TrainerStatusFlag> statusFlags) {
        this.statusFlags = statusFlags;
        return this;
    }

    // not used by EquipmentType.Bike
    private PageGen trainerDataGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            if (power > 0) {
                // non- incrementing events -> coast event
                powerEvents += 1;
            }
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


    private final TorqueData.TorqueDataPayload torqueDataPayload = new TorqueData.TorqueDataPayload();
    private PageGen torqueDataGen = new PageGen() {

        private long rotations;
        private int torqueEvents;
        private Long torqueTimeStamp = null;

        @Override
        public AntPacketEncodable getPageEncoder() {
            if (power > 0) {
                // non-incrementing event count signals a coast event
                torqueEvents += 1;
            }
            if (speed.compareTo(new BigDecimal(0.1)) < 0) {
                // assume stationary to stop divide by zero when calculating period i.e period -> infinity
                return setCommon(
                        torqueDataPayload
                                .setEvents(torqueEvents)
                );
            }

            // period for 1 rotation
            BigDecimal period = getWheelCircumference().divide(speed, 20, BigDecimal.ROUND_HALF_UP);

            if (power > 0) {
                // move this to a better place?
                setCadence(
                        new BigDecimal(60) // seconds to minutes
                                .divide(period.multiply(gearRatio), 20, BigDecimal.ROUND_HALF_UP)
                                .intValue()
                );
            } else {
                setCadence(0);
            }

            long now = System.nanoTime();
            if (torqueTimeStamp != null) {
                double delta = (now - torqueTimeStamp) / Math.pow(10,9);
                rotations += new BigDecimal(delta).divide(period, 0, BigDecimal.ROUND_HALF_UP).longValue();
            }
            torqueTimeStamp = now;

            return setCommon(
                    torqueDataPayload
                    .setEvents(torqueEvents)
                    .updateTorqueSumFromPower(power, period)
                    .setRotations(rotations)
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


    private PageGen [] normalPages = new PageGen[]
            {trainerDataGen, generalDataGen, bikeDataGen, metabolicGen, torqueDataGen};


    private PageGen basicResistanceGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            return new PercentageResistance.PercentageResistancePayload()
                    .setResistance(resistance);
        }
    };

    public void sendBasicResistance() {
        priorityMessages.add(basicResistanceGen);
    }

    private PageGen trackDataGen = new PageGen() {
        @Override
        public AntPacketEncodable getPageEncoder() {
            return TrackResistance.TrackResistancePayload.
                    from(trackResistance);
        }
    };

    public void sendTrackResistance() {
        priorityMessages.add(trackDataGen);
    }

    private PageGen windGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            return new WindResistance.WindResistancePayload()
                    .from(windResistance);
        }
    };

    public void sendWindData() {
        priorityMessages.add(windGen);
    }

    private void setCadence(int cadence) {
        //logger.debug("attempting to set cadence to: {}", cadence);
        if (cadence > 255) {
            logger.error("cadence over limit");
            cadence = 255;
        }
        this.cadence = cadence;
    }


    // type = BIKE doesn't use capabilities, torqueData
    //private RotatingView<PageGen> packetGen = new RotatingView<> (normalPages);

    private interface OperationMode extends OperationState {

        OperationMode update();

        RotatingView<PageGen> getPacketGenerators();
    }

    private OperationMode normalMode = new NormalOperationMode();

    public enum SpinDownCalibrationState {
        AWAITING_SPEED_UP,
        REACHED_SPEED,
        AWAITING_SPEED_DOWN,
    }

    public enum OperationModeState {
        BASIC_RESISTANCE,
        SIMULATION
    }

    /**
     * Read-only public representation of operating state
     */
    public interface OperationState {}

    public interface NormalOperationState extends OperationState {
        OperationModeState getMode();
    }

    public interface SpinDownInProgressState extends OperationState {
        SpinDownCalibrationState getSpinDownState();
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
    private OperationMode spinDownCalibrationMode = new SpinDownOperationMode();

    private OperationMode mode = normalMode;

    private void syncPowerModel() {
        powerModel.setTotalMass(bikeWeight.add(new BigDecimal(athlete.getWeight())).doubleValue());
        powerModel.setCoefficentRollingResistance(trackResistance.getCoefficientRollingResistance().doubleValue());
        powerModel.setGradientAsPercentage(trackResistance.getGradient().doubleValue());
        powerModel.setWindSpeed(toMetresPerSec(windResistance.getWindSpeed()));
        powerModel.updatePower(power);
        speed = new BigDecimal(powerModel.getVelocity());
    }

    private static double toMetresPerSec(int windSpeed) {
        return windSpeed / 3.6;
    }


    public void setCapabilitesRequested() {
        priorityMessages.add(capabilitiesGen);
    }

    public void setConfigRequested() {
        priorityMessages.add(configGen);
    }

    private BigDecimal getTimeSinceStart() {
        return getTimeSince(System.nanoTime(), start);
    }

    private BigDecimal getTimeSince(long now, long then) {
        return new BigDecimal(now - then)
                .divide(new BigDecimal(Math.pow(10, 9)),10, RoundingMode.HALF_UP);
    }

    private long lastDistanceTimeStamp = System.nanoTime();

    public byte [] nextPacket() {
        syncPowerModel();
        mode = mode.update();
        long now = System.nanoTime();
        distance += getTimeSince(now, lastDistanceTimeStamp).multiply(speed).intValue();
        lastDistanceTimeStamp = now;
        final byte [] packet = new byte[8];
        return doEncoding(packet);
    }

    private byte[] doEncoding(final byte[] packet) {

        if (priorityMessages.isEmpty()) {
            mode.getPacketGenerators().rotate().getPageEncoder().encode(packet);
        } else {
            AntPacketEncodable encoder = priorityMessages.remove(0).getPageEncoder();
            if (encoder != null) {
                encoder.encode(packet);
                return packet;
            }
            logger.debug("null encoder, trying next...");
            return doEncoding(packet); // skip
        }

        return packet;
    }

    private void onCmdReceieved(CommandId commandId) {
        lastCmd = commandId;
        seqNum.add(1);
        if (mode instanceof NormalOperationMode) {
            NormalOperationMode mode = ((NormalOperationMode) this.mode);
            switch (commandId) {
                case BASIC_RESISTANCE:
                    mode.setMode(OperationModeState.BASIC_RESISTANCE);
                case TRACK_RESISTANCE:
                case WIND_RESISTANCE:
                    mode.setMode(OperationModeState.SIMULATION);
            }
        }
    }


    public void useConfig(Config config) {
        // we don't get sent sex, height,
        athlete = new MaleAthlete(ATHLETE_HEIGHT, config.getUserWeight().doubleValue(), ATHLETE_AGE);
        bikeWeight = config.getBicycleWeight();
        gearRatio = config.getGearRatio();
        wheelDiameter = config.getBicycleWheelDiameter();
    }

    public void sendCmdStatus() {
        priorityMessages.add(cmdStatusGen);
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


    private class NormalOperationMode implements OperationMode, NormalOperationState {

        private OperationModeState mode = OperationModeState.BASIC_RESISTANCE;

        private void setMode(OperationModeState mode) {
            this.mode = mode;
        }

        private RotatingView<PageGen> normalPackets = new RotatingView<>(normalPages);

        @Override
        public OperationMode update() {
            return this;
        }

        @Override
        public RotatingView<PageGen> getPacketGenerators() {
            return normalPackets;
        }

        @Override
        public OperationModeState getMode() {
            return mode;
        }
    }

    private static BigDecimal toKmh(BigDecimal ms) {
        return ms.multiply(new BigDecimal(3.6));
    }

    private class SpinDownOperationMode implements OperationMode, SpinDownInProgressState {

        private SpinDownCalibrationState state = SpinDownCalibrationState.AWAITING_SPEED_UP;

        private final PageGen calibrationStatusGen = new PageGen() {
            @Override
            public AntPacketEncodable getPageEncoder() {
                return new CalibrationProgress.CalibrationProgressPayload()
                        .setTemp(internalTemp)
                        .setTempState(Defines.TemperatureCondition.CURRENT_TEMPERATURE_OK)
                        .setSpeedState(getSpeedState())
                        .setSpinDownPending(true)
                        .setTargetSpeed(toKmh(TARGET_SPEED))
                        .setTargetSpinDownTime(10000);
            }

        };

        private PageGen [] calibrationPages;

        {
            // this needs normalPages to be defined before this in the source file
            List<PageGen> t = new ArrayList<>(Arrays.asList(normalPages));
            t.add(calibrationStatusGen);
            calibrationPages = t.toArray(new PageGen[0]);

        }



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

        @Override
        public SpinDownCalibrationState getSpinDownState() {
            return state;
        }
    }
}
