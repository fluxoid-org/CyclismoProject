package org.cowboycoders.ant.profiles.simulators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cowboycoders.ant.profiles.fitnessequipment.*;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines.CommandId;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.*;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.GeneralData.GeneralDataPayload;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.CommonCommandPage;
import org.fluxoid.utils.MathCompat;
import org.fluxoid.utils.RollOverVal;
import org.fluxoid.utils.RotatingView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.PI;

/**
 * Created by fluxoid on 01/02/17.
 */
public class DummyTrainerState {

    private Logger logger = LogManager.getLogger();

    // 700c http://www.bikecalc.com/wheel_size_math
    private BigDecimal wheelDiameter = new BigDecimal(0.700).divide(new BigDecimal(PI),4, RoundingMode.HALF_UP);
    private BigDecimal resistance = new BigDecimal(0);

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
            // period for 1 rotation
            BigDecimal period = getWheelCircumference().divide(speed, 20, BigDecimal.ROUND_HALF_UP);
            torqueEvents += 1;
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


    private PageGen metabolicGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            long timeNanos = System.nanoTime() - start;
            double timeHrs = timeNanos / Math.pow(10, 9) / (60 * 60);
            BigDecimal instantCalorieBurn = athlete.getEstimatedCalorificBurn(MET_CYCLING);
            BigDecimal calsBurnt = instantCalorieBurn.multiply(new BigDecimal(timeHrs));
            return setCommon(
                    new MetabolicData.MetabolicDataPayload()
                            .setInstantCalorieBurn(instantCalorieBurn)
                            .setCalorieCounter(calsBurnt.intValue())
                            .setInstantMetabolicEquivalents(new BigDecimal(MET_CYCLING)

            ));
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


    // type = BIKE doesn't use capabilities, torqueData
    private RotatingView<PageGen> packetGen = new RotatingView<> (
            new PageGen [] {generalDataGen, bikeDataGen, metabolicGen, torqueDataGen}
    );

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

        distance = getTimeSinceStart().multiply(speed).intValue();
        final byte [] packet = new byte[8];
        if (priorityMessages.isEmpty()) {
            packetGen.rotate().getPageEncoder().encode(packet);
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
}
