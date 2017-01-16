package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.HasPower;

import java.math.BigDecimal;

/**
 * Instantaneous power
 * Created by fluxoid on 05/01/17.
 */
public class InstantPowerUpdate implements HasPower {

    @Override
    public BigDecimal getPower() {
        return power;
    }

    public InstantPowerUpdate(BigDecimal power) {
        this.power = power;
    }

    private final BigDecimal power;
}
