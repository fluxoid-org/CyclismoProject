package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 08/02/17.
 */
public class DistanceUpdate extends TaggedTelemetryEvent {
    private final BigDecimal distance;

    /**
     *
     * @param distance in m
     */
    public DistanceUpdate(Object tag, BigDecimal distance) {
        super(tag);
        this.distance = distance;
    }

    /**
     *
     * @param distance in m
     */
    public DistanceUpdate(Object tag, long distance) {
        this(tag, new BigDecimal(distance));
    }

    /** in m **/
    public BigDecimal getDistance() {
        return distance;
    }
}
