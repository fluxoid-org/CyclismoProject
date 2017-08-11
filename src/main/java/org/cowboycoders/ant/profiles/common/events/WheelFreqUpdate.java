package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 16/01/17.
 */
public class WheelFreqUpdate extends TaggedTelemetryEvent {
    private BigDecimal rotationalFrequency;

    /**
     *
     * @param rotationalFrequency in rotations/seconds
     */
    public WheelFreqUpdate(Object tag, BigDecimal rotationalFrequency) {
        super(tag);
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
