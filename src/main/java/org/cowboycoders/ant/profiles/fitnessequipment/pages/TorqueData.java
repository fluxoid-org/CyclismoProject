package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.TimeOutDeltaValidator;
import org.cowboycoders.ant.profiles.common.decode.interfaces.*;
import org.cowboycoders.ant.profiles.common.utils.CounterUtils;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.util.concurrent.TimeUnit;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Created by fluxoid on 09/01/17.
 */
public class TorqueData extends CommonPageData implements AntPage, TorqueDecodable, DistanceDecodable, SpeedDecodable {

    public static int PAGE_NUMBER = 26;

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

    public static class TorqueDataPayload {
        private int events;
        private int torqueSum;

        // wheel related
        private int rotations;
        private int period;

        public int getEvents() {
            return events;
        }

        public TorqueDataPayload setEvents(int events) {
            this.events = events;
            return this;
        }

        public int getTorqueSum() {
            return torqueSum;
        }

        public TorqueDataPayload setTorqueSum(int torqueSum) {
            this.torqueSum = torqueSum;
            return this;
        }

        public int getRotations() {
            return rotations;
        }

        public TorqueDataPayload setRotations(int rotations) {
            this.rotations = rotations;
            return this;
        }

        public int getPeriod() {
            return period;
        }

        public TorqueDataPayload setPeriod(int period) {
            this.period = period;
            return this;
        }


        public void encode(final byte[] packet) {
            PutUnsignedNumIn1LeBytes(packet, PAGE_OFFSET, PAGE_NUMBER);
            PutUnsignedNumIn1LeBytes(packet, EVENT_OFFSET, events);
            PutUnsignedNumIn1LeBytes(packet, ROTATION_OFFSET, rotations);
            PutUnsignedNumIn2LeBytes(packet, PERIOD_OFFSET, period);
            PutUnsignedNumIn2LeBytes(packet, TORQUE_OFFSET, torqueSum);

        }
    }


    public TorqueData(byte[] packet) {
        super(packet);
        timestamp = System.nanoTime();
        events = UnsignedNumFrom1LeByte(packet[EVENT_OFFSET]);
        rotations = UnsignedNumFrom1LeByte(packet[ROTATION_OFFSET]);
        period = UnsignedNumFrom2LeBytes(packet, PERIOD_OFFSET);
        torqueSum = UnsignedNumFrom2LeBytes(packet, TORQUE_OFFSET);
    }

    @Override
    public long getTorqueDelta(TorqueDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT16_MAX, old.getTorque(), getTorque());
    }

    @Override
    public int getTorque() {
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
    public long getWheelRotationsDelta(DistanceDecodable old) {
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
