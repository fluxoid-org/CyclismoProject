package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * p49
 * Created by fluxoid on 16/01/17.
 */
public class TargetPower implements AntPage {

    private static final int POWER_OFFSET = 6;
    private final BigDecimal targetPower;

    public TargetPower(byte[] packet) {
        int raw = BitManipulation.UnsignedNumFrom2LeBytes(packet, POWER_OFFSET);
        targetPower = new BigDecimal(raw).divide(new BigDecimal(4), 2, RoundingMode.HALF_UP);
    }

    /**
     * Target power (w) in range 0-1000, resolution: 0.25
     */
    public BigDecimal getTargetPower() {
        return targetPower;
    }
}
