package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

/**
 * Created by fluxoid on 10/01/17.
 */
public class WheelRotationsUpdate extends TaggedTelemetryEvent {

    public WheelRotationsUpdate(Object tag, long wheelRotations) {
        super(tag);
        this.wheelRotations = wheelRotations;
    }

    final long wheelRotations;

    public long getWheelRotations() {
        return wheelRotations;
    }
}
