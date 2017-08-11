package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.HasAveragePower;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 10/01/17.
 */
public class AverageTorqueUpdate extends TaggedTelemetryEvent implements HasAveragePower {

    private final long events;
    private long periodSum;
    private long torqueSum;

    public long getPeriodSum() {
        return periodSum;
    }

    public long getTorqueSum() {
        return torqueSum;
    }

    public long getEvents() {
        return events;
    }

    public AverageTorqueUpdate(Object tag, long periodSum, long torqueSum, long events) {
        super(tag);
        this.periodSum = periodSum;
        this.torqueSum = torqueSum;
        this.events = events;
    }

    @Override
    public BigDecimal getAveragePower() {
        if (torqueSum == 0) {
            return new BigDecimal(0);
        }
        return new BigDecimal(128).multiply(new BigDecimal(3.141592653589793))
                .
                multiply(new BigDecimal(torqueSum)
                        .divide(new BigDecimal(periodSum), 9, RoundingMode.HALF_UP));
    }

    public BigDecimal getAverageTorque() {
        return new BigDecimal(torqueSum).divide(new BigDecimal(32), 15, RoundingMode.HALF_UP).divide(new BigDecimal(events), 13, RoundingMode.HALF_UP);
    }
}
