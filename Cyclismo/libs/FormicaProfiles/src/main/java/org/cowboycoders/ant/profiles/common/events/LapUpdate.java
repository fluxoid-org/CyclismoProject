package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

public class LapUpdate extends TaggedTelemetryEvent {

    private final int laps;

    public LapUpdate(Object tag, int laps) {
        super(tag);
        this.laps = laps;
    }

    public int getLaps() {
        return laps;
    }
}
