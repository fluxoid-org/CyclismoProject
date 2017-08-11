package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 10/01/17.
 */
public class TorqueUpdate extends TaggedTelemetryEvent {

    public BigDecimal getTorque() {
        return torque;
    }

    public TorqueUpdate(Object tag, BigDecimal torque) {
        super(tag);
        this.torque = torque;
    }

    private final BigDecimal torque;
}
