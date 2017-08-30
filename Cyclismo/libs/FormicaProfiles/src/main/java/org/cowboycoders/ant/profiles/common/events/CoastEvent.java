package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

public abstract class CoastEvent extends TaggedTelemetryEvent {
    protected CoastEvent(Object tag) {
        super(tag);
    }
}
