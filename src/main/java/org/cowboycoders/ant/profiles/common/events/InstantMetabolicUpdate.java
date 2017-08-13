package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.HasCalories;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;

public class InstantMetabolicUpdate extends TaggedTelemetryEvent {


    private final BigDecimal instantCalorieBurn;
    private final BigDecimal instantMetabolicEquivalent;

    public InstantMetabolicUpdate(Object tag, BigDecimal instantCalorieBurn, BigDecimal instantMetabolicEquivalent) {
        super(tag);
        this.instantCalorieBurn = instantCalorieBurn;
        this.instantMetabolicEquivalent = instantMetabolicEquivalent;

    }

    public BigDecimal getInstantCalorieBurn() {
        return instantCalorieBurn;
    }

    public BigDecimal getInstantMetabolicEquivalent() {
        return instantMetabolicEquivalent;
    }
}
