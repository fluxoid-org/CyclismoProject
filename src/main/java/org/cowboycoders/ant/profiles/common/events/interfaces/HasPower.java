package org.cowboycoders.ant.profiles.common.events.interfaces;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 10/01/17.
 */
public interface HasPower extends TelemetryEvent {
    BigDecimal getPower();
}
