package org.org.cowboycoders.ant.profiles.simulators;

import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.BikeData;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.CommonPageData;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.GeneralData;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.fluxoid.utils.RotatingView;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Created by fluxoid on 01/02/17.
 */
public class DummyTrainerState {

    private boolean lapFlag;
    private int power;
    private int cadence;
    private BigDecimal speed = new BigDecimal(0.0);
    private Defines.EquipmentState state = Defines.EquipmentState.READY;
    private static final Defines.EquipmentType type = Defines.EquipmentType.BIKE;

    private <V extends CommonPageData.CommonPagePayload> V setCommon(V payload) {
        payload.setLapFlag(lapFlag)
                .setState(state);
        return payload;
    }

    private Integer distance = 0;
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

    public DummyTrainerState setDistance(Integer distance) {
        this.distance = distance;
        return this;
    }

    public DummyTrainerState setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
        return this;
    }

    public Integer getDistance() {
        return distance;
    }

    /**
     * the assumption is that this is called infrequently, so we
     * get a change to transmit the old flag;
     */
    public void incrementLaps() {
        lapFlag =! lapFlag;
    }

    private PageGen generalDataGen = new PageGen() {

        @Override
        public AntPacketEncodable getPageEncoder() {
            long now =  System.nanoTime();
            BigDecimal elapsed = new BigDecimal((now - start) / Math.pow(10,9));
            return setCommon(
                    new GeneralData.GeneralDataPayload()
                    .setType(Defines.EquipmentType.BIKE)
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
    private RotatingView<PageGen> packetGen = new RotatingView<> (
            new PageGen [] {generalDataGen, bikeDataGen}
    );

    public byte [] nextPacket() {
        final byte [] packet = new byte[8];
        packetGen.rotate().getPageEncoder().encode(packet);
        return packet;
    }
}
