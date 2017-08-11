package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 16/01/17.
 */
public class SpeedUpdate extends TaggedTelemetryEvent {

    private final boolean isVirtual;

    /**
     * @return km/h
     */
    public BigDecimal getSpeed() {
        return speed;
    }

    public SpeedUpdate(Object tag, BigDecimal speed, boolean isVirtual) {
        super(tag);
        this.speed = speed;
        this.isVirtual = isVirtual;
    }

    private final BigDecimal speed;

}
