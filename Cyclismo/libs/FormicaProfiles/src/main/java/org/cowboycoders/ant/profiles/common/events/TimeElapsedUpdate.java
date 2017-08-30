package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 27/01/17.
 */
public class TimeElapsedUpdate extends TaggedTelemetryEvent {

    final BigDecimal timeElapsed;

    public BigDecimal getTimeElapsed() {
        return timeElapsed;
    }

    public TimeElapsedUpdate(Object tag, BigDecimal timeElapsed) {
        super(tag);
        this.timeElapsed = timeElapsed;
    }
}
