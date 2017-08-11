package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.HasPower;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;

/**
 * Instantaneous power
 * Created by fluxoid on 05/01/17.
 */
public class InstantPowerUpdate extends TaggedTelemetryEvent implements HasPower  {

    @Override
    public BigDecimal getPower() {
        return power;
    }

    public InstantPowerUpdate(Object tag, BigDecimal power) {
        super(tag);
        this.power = power;
    }

    private final BigDecimal power;
}
