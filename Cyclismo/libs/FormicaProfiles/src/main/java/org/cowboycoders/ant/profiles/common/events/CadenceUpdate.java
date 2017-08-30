package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

public class CadenceUpdate extends TaggedTelemetryEvent {
    private final int cadence;

    public CadenceUpdate(Object tag, int cadence) {
        super(tag);
        this.cadence = cadence;
    }

    public int getCadence() {
        return cadence;
    }
}
