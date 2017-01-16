package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.HasAveragePower;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 05/01/17.
 */
public class AveragedPowerUpdate implements TelemetryEvent, HasAveragePower {
    private final long events;
    private final long accumPower;

    @Override
    public BigDecimal getAveragePower() {
        return new BigDecimal(accumPower)
                .divide(new BigDecimal(events),
                        5, RoundingMode.HALF_UP);
    }

    public AveragedPowerUpdate(long accumPower, long events) {
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
