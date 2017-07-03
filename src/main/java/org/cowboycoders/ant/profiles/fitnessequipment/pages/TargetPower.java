package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.PutUnsignedNumIn1LeBytes;
import static org.cowboycoders.ant.profiles.BitManipulation.PutUnsignedNumIn2LeBytes;

/**
 * p49
 * Created by fluxoid on 16/01/17.
 */
public class TargetPower implements AntPage {

    public static final int PAGE_NUMBER = 49;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    private static final int POWER_OFFSET = 6;
    private final BigDecimal targetPower;

    public static class TargetPowerPayload {

        private BigDecimal targetPower;


        public BigDecimal getTargetPower() {
            return targetPower;
        }

        public TargetPowerPayload setTargetPower(BigDecimal targetPower) {
            this.targetPower = targetPower;
            return this;
        }

        public void encode(final byte[] packet)
        {
            PutUnsignedNumIn1LeBytes(packet, PAGE_OFFSET, PAGE_NUMBER);
            if (targetPower == null) {
                throw new IllegalArgumentException("must set targetPower");
            }
            BigDecimal n = targetPower.multiply(new BigDecimal(4)).setScale(0, RoundingMode.HALF_UP);
            PutUnsignedNumIn2LeBytes(packet, POWER_OFFSET, n.intValue());
        }



    }

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
