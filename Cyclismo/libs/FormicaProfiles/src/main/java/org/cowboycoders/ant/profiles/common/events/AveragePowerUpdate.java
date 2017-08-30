package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.HasAveragePower;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 05/01/17.
 */
public class AveragePowerUpdate extends TaggedTelemetryEvent implements HasAveragePower {
    private final long events;
    private final long accumPower;

    @Override
    public BigDecimal getAveragePower() {
        if (events == 0) return new BigDecimal(0.0);
        return new BigDecimal(accumPower)
                .divide(new BigDecimal(events),
                        5, RoundingMode.HALF_UP);
    }

    public AveragePowerUpdate(Object tag, long accumPower, long events) {
        super(tag);
        this.accumPower = accumPower;
        this.events = events;
    }

    public long getEvents() {
        return events;
    }

    public long getSumPower() {
        return accumPower;
    }
}
