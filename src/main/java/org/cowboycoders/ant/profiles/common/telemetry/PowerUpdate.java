package org.cowboycoders.ant.profiles.common.telemetry;

import java.math.BigDecimal;

/**
 * Instantaneous power
 * Created by fluxoid on 05/01/17.
 */
public class PowerUpdate implements TelemetryEvent {
    public BigDecimal getPower() {
        return power;
    }

    public PowerUpdate(BigDecimal power) {
        this.power = power;
    }

    private final BigDecimal power;
}
