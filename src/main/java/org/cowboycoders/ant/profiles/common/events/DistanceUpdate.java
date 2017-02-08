package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 08/02/17.
 */
public class DistanceUpdate implements TelemetryEvent{
    private final BigDecimal distance;

    /**
     *
     * @param distance in m
     */
    public DistanceUpdate(BigDecimal distance) {
        this.distance = distance;
    }

    /** in m **/
    public BigDecimal getDistance() {
        return distance;
    }
}
