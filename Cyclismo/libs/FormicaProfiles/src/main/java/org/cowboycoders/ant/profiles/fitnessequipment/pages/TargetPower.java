package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public static class TargetPowerPayload implements AntPacketEncodable {

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
            LittleEndianArray viewer = new LittleEndianArray(packet);
            viewer.putUnsigned(PAGE_OFFSET, 1, PAGE_NUMBER);
            if (targetPower == null) {
                throw new IllegalArgumentException("must set targetPower");
            }
            BigDecimal n = targetPower.multiply(new BigDecimal(4)).setScale(0, RoundingMode.HALF_UP);
            viewer.putUnsigned(POWER_OFFSET,2, n.intValue());
        }



    }

    public TargetPower(byte[] packet) {
        LittleEndianArray viewer = new LittleEndianArray(packet);
        int raw = viewer.unsignedToInt(POWER_OFFSET, 2);
        targetPower = new BigDecimal(raw).divide(new BigDecimal(4), 2, RoundingMode.HALF_UP);
    }

    /**
     * Target power (w) in range 0-1000, resolution: 0.25
     */
    public BigDecimal getTargetPower() {
        return targetPower;
    }
}
