package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

public class CoastEndEvent extends TaggedTelemetryEvent {
    public CoastEndEvent(Object tag) {
        super(tag);
    }
}
