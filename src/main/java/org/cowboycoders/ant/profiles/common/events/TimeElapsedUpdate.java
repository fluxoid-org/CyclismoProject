package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 27/01/17.
 */
public class TimeElapsedUpdate implements TelemetryEvent {

    final BigDecimal timeElapsed;

    public BigDecimal getTimeElapsed() {
        return timeElapsed;
    }

    public TimeElapsedUpdate(BigDecimal timeElapsed) {
        this.timeElapsed = timeElapsed;
    }
}
