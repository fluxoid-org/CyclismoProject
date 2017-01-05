package org.cowboycoders.ant.profiles.common.telemetry;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 05/01/17.
 */
public class AveragedPowerUpdate implements TelemetryEvent {
    private final long events;
    private final long accumPower;

    public BigDecimal getPower() {
        return new BigDecimal(accumPower)
                .divide(new BigDecimal(events),
                        5, RoundingMode.HALF_UP);
    }

    public AveragedPowerUpdate(long accumPower, long events) {
        this.accumPower = accumPower;
        this.events = events;
    }

}
