package org.cowboycoders.ant.profiles.common.decode.power;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 04/01/17.
 */
public interface PowerListener {
    boolean onCoastDetected();
    BigDecimal onPowerUpdate(BigDecimal power);
}
