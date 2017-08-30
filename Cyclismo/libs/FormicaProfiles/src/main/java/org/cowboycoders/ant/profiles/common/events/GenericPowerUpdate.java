package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.HasPower;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;


public abstract class GenericPowerUpdate extends TaggedTelemetryEvent implements HasPower {
    protected GenericPowerUpdate(Object tag) {
        super(tag);
    }
}
