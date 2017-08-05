package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

public class LapUpdate implements TelemetryEvent {

    private final int laps;

    public LapUpdate(int laps) {
        this.laps = laps;
    }

    public int getLaps() {
        return laps;
    }
}
