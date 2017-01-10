package org.cowboycoders.ant.profiles.common.events;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 10/01/17.
 */
public interface PowerUpdate extends TelemetryEvent {
    BigDecimal getPower();
}
