package org.cowboycoders.ant.profiles.common.telemetry;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 10/01/17.
 */
public class TorquePowerUpdate  implements PowerUpdate {

    public static final double PI = 3.141592653589793;
    private final long torqueSum;
    private final long period;

    public TorquePowerUpdate(long torqueSum, long period) {
        this.torqueSum = torqueSum;
        this.period = period;
    }

    @Override
    public BigDecimal getPower() {
        return new BigDecimal(128).multiply(new BigDecimal(PI)).multiply(new BigDecimal(torqueSum)).divide(new BigDecimal(period), 9, RoundingMode.HALF_UP);
    }
}
