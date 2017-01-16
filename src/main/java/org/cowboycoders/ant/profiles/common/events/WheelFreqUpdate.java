package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 16/01/17.
 */
public class WheelFreqUpdate implements TelemetryEvent {
    private BigDecimal rotationalFrequency;

    public WheelFreqUpdate(BigDecimal rotationalFrequency) {
        this.rotationalFrequency = rotationalFrequency;
    }

    public BigDecimal getRotationalFrequency() {
        return rotationalFrequency;
    }
}
