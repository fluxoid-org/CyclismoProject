package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.TimeOutDeltaValidator;
import org.cowboycoders.ant.profiles.common.decode.interfaces.*;
import org.cowboycoders.ant.profiles.common.utils.CounterUtils;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.MathCompat;
import org.fluxoid.utils.RollOverVal;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.PI;
import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Created by fluxoid on 09/01/17.
 */
public class TorqueData extends CommonPageData implements AntPage, TorqueDecodable, RotationsToDistanceDecodable, SpeedDecodable {

    public static final int PAGE_NUMBER = 26;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    private static final int EVENT_OFFSET = 1;
    private static final int ROTATION_OFFSET = 2;
    private static final int PERIOD_OFFSET = 3;
    private static final int TORQUE_OFFSET = 5;
    private static final long TIMEOUT_DELTA = TimeUnit.SECONDS.toNanos(12);

    private final int events;
    private final int torqueSum;

    // wheel related
    private final int rotations;
    private final int period;
    private final long timestamp;
    private final TimeOutDeltaValidator timeOutDeltaValidator = new TimeOutDeltaValidator(TIMEOUT_DELTA);

    public static class TorqueDataPayload extends CommonPagePayload  implements AntPacketEncodable {
        RollOverVal events = new RollOverVal(UNSIGNED_INT8_MAX);
        RollOverVal torqueSum = new RollOverVal(UNSIGNED_INT16_MAX);

        // wheel related
        RollOverVal rotations = new RollOverVal(UNSIGNED_INT8_MAX);
        RollOverVal period = new RollOverVal(UNSIGNED_INT16_MAX);

        public int getEvents() {
            return MathCompat.toIntExact(events.getValue());
        }

        public TorqueDataPayload setEvents(int events) {
            this.events.setValue(events);
            return this;
        }

        public long getTorqueSum() {
            return torqueSum.getValue();
        }

        public TorqueDataPayload setTorqueSum(int torqueSum) {
            this.torqueSum.setValue(torqueSum);
            return this;
        }

        public TorqueDataPayload setTorqueSum(BigDecimal torqueSum) {
            BigDecimal scaled = torqueSum.multiply(new BigDecimal(32)).setScale(0, RoundingMode.HALF_UP);
            this.torqueSum.setValue(scaled.longValue());
            return this;
        }


        public TorqueDataPayload updateTorqueSumFromPower(int power, BigDecimal periodInSeconds) {
            BigDecimal scaledPeriod = periodInSeconds.multiply(new BigDecimal(2048));
            this.period.add(scaledPeriod.longValue());
            BigDecimal delta = new BigDecimal(power).multiply(scaledPeriod)
                    .divide(new BigDecimal(PI).multiply(new BigDecimal(128)), 0, BigDecimal.ROUND_HALF_UP);
            this.torqueSum.add(delta.longValue());
            return this;

        }

        public long getRotations() {
            return rotations.getValue();
        }

        public TorqueDataPayload setRotations(long rotations) {
            this.rotations.setValue(rotations);
            return this;
        }

        public long getPeriod() {
            return period.getValue();
        }

        public TorqueDataPayload setPeriod(long period) {
            this.period.setValue(period);
            return this;
        }

        @Override
        public TorqueDataPayload setLapFlag(boolean lapflag) {
            return (TorqueDataPayload) super.setLapFlag(lapflag);
        }

        @Override
        public TorqueDataPayload  setState(Defines.EquipmentState state) {
            return (TorqueDataPayload) super.setState(state);
        }

        public void encode(final byte[] packet) {
            super.encode(packet);
            LittleEndianArray viewer = new LittleEndianArray(packet);
            viewer.putUnsigned(PAGE_OFFSET, 1, PAGE_NUMBER);
            viewer.putUnsigned(EVENT_OFFSET, 1, MathCompat.toIntExact(events.get()));
            viewer.putUnsigned(ROTATION_OFFSET, 1, MathCompat.toIntExact(rotations.get()));
            viewer.putUnsigned(PERIOD_OFFSET,2, MathCompat.toIntExact(period.get()));
            viewer.putUnsigned(TORQUE_OFFSET,2, MathCompat.toIntExact(torqueSum.get()));

        }
    }


    public TorqueData(byte[] packet) {
        super(packet);
        LittleEndianArray viewer = new LittleEndianArray(packet);
        timestamp = System.nanoTime();
        events = viewer.unsignedToInt(EVENT_OFFSET, 1);
        rotations = viewer.unsignedToInt(ROTATION_OFFSET, 1);
        period = viewer.unsignedToInt(PERIOD_OFFSET, 2);
        torqueSum = viewer.unsignedToInt(TORQUE_OFFSET, 2);
    }

    @Override
    public long getRawTorqueDelta(TorqueDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT16_MAX, old.getRawTorque(), getRawTorque());
    }

    @Override
    public int getRawTorque() {
        return torqueSum;
    }


    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getEventCountDelta(CounterBasedDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT8_MAX, old.getEventCount(), getEventCount());
    }

    @Override
    public int getEventCount() {
        return events;
    }

    @Override
    public boolean isValidDelta(CounterBasedDecodable old) {
        return timeOutDeltaValidator.isValidDelta(old, this);
    }

    @Override
    public int getWheelRotations() {
        return rotations;
    }

    @Override
    public long getWheelRotationsDelta(RotationsToDistanceDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT8_MAX, old.getWheelRotations(), getWheelRotations());
    }

    @Override
    public int getRotationPeriod() {
        return period;
    }

    @Override
    public long getRotationPeriodDelta(RotationDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT16_MAX, old.getRotationPeriod(), getRotationPeriod());
    }
}
