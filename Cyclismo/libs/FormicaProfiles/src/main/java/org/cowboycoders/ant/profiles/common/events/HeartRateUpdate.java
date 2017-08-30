package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;

public class HeartRateUpdate extends TaggedTelemetryEvent {
    private final int hr;
    private final Defines.HeartRateDataSource source;

    public HeartRateUpdate(Object tag, Defines.HeartRateDataSource source, int hr) {
        super(tag);
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
