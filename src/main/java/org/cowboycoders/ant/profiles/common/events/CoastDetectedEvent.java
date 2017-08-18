package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

/**
 * Created by fluxoid on 05/01/17.
 */
public class CoastDetectedEvent extends CoastEvent {

    public CoastDetectedEvent(Object tag) {
        super(tag);
    }
}
