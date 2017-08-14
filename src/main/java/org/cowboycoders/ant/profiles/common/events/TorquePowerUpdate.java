package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.HasPower;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.Math.PI;

/**
 * Created by fluxoid on 10/01/17.
 */
public class TorquePowerUpdate extends GenericPowerUpdate {

    private final long torqueSum;
    private final long period;

    public TorquePowerUpdate(Object tag, long torqueSum, long period) {
        super(tag);
        this.torqueSum = torqueSum;
        this.period = period;
    }

    @Override
    public BigDecimal getPower() {
        if (period == 0) {
            return new BigDecimal(0);
        }
        return new BigDecimal(128).multiply(new BigDecimal(PI)).multiply(new BigDecimal(torqueSum)).divide(new BigDecimal(period), 9, RoundingMode.HALF_UP);
    }
}
