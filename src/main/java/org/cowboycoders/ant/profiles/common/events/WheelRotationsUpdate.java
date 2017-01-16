package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

/**
 * Created by fluxoid on 10/01/17.
 */
public class WheelRotationsUpdate  implements TelemetryEvent {

    public WheelRotationsUpdate(long wheelRotations) {
        this.wheelRotations = wheelRotations;
    }

    final long wheelRotations;

    public long getWheelRotations() {
        return wheelRotations;
    }
}
