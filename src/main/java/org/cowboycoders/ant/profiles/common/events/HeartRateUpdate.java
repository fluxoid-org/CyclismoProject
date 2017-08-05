package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;

public class HeartRateUpdate implements TelemetryEvent {
    private final int hr;
    private final Defines.HeartRateDataSource source;

    public HeartRateUpdate(Defines.HeartRateDataSource source, int hr) {
        this.hr = hr;
        this.source = source;
    }

    /**
     *
     * @return in bpm
     */
    public int getHeartRate() {
        return hr;
    }
}
