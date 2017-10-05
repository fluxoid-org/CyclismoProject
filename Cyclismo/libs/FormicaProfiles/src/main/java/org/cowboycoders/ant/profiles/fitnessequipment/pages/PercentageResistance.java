package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.pages.SinglePacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.math.BigDecimal;

/**
 * Page 48
 * Created by fluxoid on 16/01/17.
 */
public class PercentageResistance implements AntPage {

    public static final  int PAGE_NUMBER = 48;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    private static final int RESISTANCE_OFFSET = 7;
    private final BigDecimal resistance;

    public static class PercentageResistancePayload implements SinglePacketEncodable {
        private BigDecimal resistance = new BigDecimal(0);

        public BigDecimal getResistance() {
            return resistance;
        }

        public PercentageResistancePayload setResistance(BigDecimal resistance) {
            if (resistance == null) {
                throw new IllegalArgumentException("resistance cannot be null");
            }
            this.resistance = resistance;
            return this;
        }

        public void encode(final byte [] packet) {
            LittleEndianArray viewer = new LittleEndianArray(packet);
            viewer.putUnsigned(PAGE_OFFSET, 1, PAGE_NUMBER);
            BigDecimal n = resistance.multiply(new BigDecimal(2));
            viewer.putUnsigned(RESISTANCE_OFFSET, 1, n.intValue());

        }
    }

    public PercentageResistance(byte[] packet) {
        LittleEndianArray viewer = new LittleEndianArray(packet);
        int raw = viewer.unsignedToInt(RESISTANCE_OFFSET, 1);
        resistance = new BigDecimal(raw).divide(new BigDecimal(2));
    }

    /**
     * Percentage resistance
     * @return 0-100%, resolution 0.5%
     */
    public BigDecimal getResistance() {
        return resistance;
    }
}
