package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 16/01/17.
 */
public class WheelFreqUpdate implements TelemetryEvent {
    private BigDecimal rotationalFrequency;

    /**
     *
     * @param rotationalFrequency in rotations/seconds
     */
    public WheelFreqUpdate(BigDecimal rotationalFrequency) {
        this.rotationalFrequency = rotationalFrequency;
    }

    /**
     *
     * @return in rotations/second
     */
    public BigDecimal getRotationalFrequency() {
        return rotationalFrequency;
    }
}
