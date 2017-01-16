package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.TimeOutDeltaValidator;
import org.cowboycoders.ant.profiles.common.decode.interfaces.DistanceDecodable;
import org.cowboycoders.ant.profiles.common.utils.CounterUtils;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CounterBasedDecodable;
import org.cowboycoders.ant.profiles.common.decode.interfaces.TorqueDecodable;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.util.concurrent.TimeUnit;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Created by fluxoid on 09/01/17.
 */
public class TorqueData extends CommonPageData implements AntPage, TorqueDecodable, DistanceDecodable {

    private static final int EVENT_OFFSET = 2;
    private static final int ROTATION_OFFSET = 3;
    private static final int PERIOD_OFFSET = 4;
    private static final int TORQUE_OFFSET = 6;
    private static final int SOURCE_OFFSET = 1;
    private static final long TIMEOUT_DELTA = TimeUnit.SECONDS.toNanos(12);

    private final int events;
    private final int torqueSum;

    // wheel related
    private final int rotations;
    private final int period;
    private final Defines.TorqueSource source;
    private final long timestamp;
    private final TimeOutDeltaValidator timeOutDeltaValidator = new TimeOutDeltaValidator(TIMEOUT_DELTA);


    public TorqueData(byte[] packet) {
        super(packet);
        timestamp = System.nanoTime();
        events = UnsignedNumFrom1LeByte(packet[EVENT_OFFSET]);
        rotations = UnsignedNumFrom1LeByte(packet[ROTATION_OFFSET]);
        period = UnsignedNumFrom2LeBytes(packet, PERIOD_OFFSET);
        torqueSum = UnsignedNumFrom2LeBytes(packet, TORQUE_OFFSET);
        source = Defines.TorqueSource.getValueFromInt(UnsignedNumFrom1LeByte(packet[SOURCE_OFFSET]));
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
    public int getPeriod() {
        return period;
    }

    @Override
    public long getPeriodDelta(TorqueDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT16_MAX, old.getPeriod(), getPeriod());
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
}
