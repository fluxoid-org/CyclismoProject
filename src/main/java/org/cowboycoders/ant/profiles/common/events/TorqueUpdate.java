package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 10/01/17.
 */
public class TorqueUpdate implements TelemetryEvent {

    public BigDecimal getTorque() {
        return torque;
    }

    public TorqueUpdate(BigDecimal torque) {
        this.torque = torque;
    }

    private final BigDecimal torque;
}
