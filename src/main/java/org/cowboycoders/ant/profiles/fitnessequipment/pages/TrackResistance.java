package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * p51
 * Created by fluxoid on 16/01/17.
 */
public class TrackResistance implements AntPage {

    private final BigDecimal DEFAULT_ROLLING_RESISTANCE = new BigDecimal(0.004);
    private final BigDecimal DEFAULT_GRADIENT = new BigDecimal(0.00);
    private static final int GRADIENT_OFFSET = 5;
    private static final int ROLLING_OFFSET = 7;
    private final BigDecimal gradient;
    private final BigDecimal coefficientRollingResistance;

    public TrackResistance(byte[] packet) {
        int gradRaw = BitManipulation.UnsignedNumFrom2LeBytes(packet, GRADIENT_OFFSET);
        if (gradRaw != BitManipulation.UNSIGNED_INT16_MAX) {
            gradient = new BigDecimal(gradRaw).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                    .subtract(new BigDecimal(200));
        } else {
            gradient = DEFAULT_GRADIENT;
        }
        int rollingRaw = BitManipulation.UnsignedNumFrom1LeByte(packet[ROLLING_OFFSET]);
        if (rollingRaw != BitManipulation.UNSIGNED_INT8_MAX) {
            coefficientRollingResistance = new BigDecimal(rollingRaw)
                    .divide(new BigDecimal(20000), 5, RoundingMode.HALF_UP);
        } else {
            coefficientRollingResistance = DEFAULT_ROLLING_RESISTANCE;
        }


    }

    /**
     * slope of the terrain in range -200% to 200%, resolution 0.01
     */
    public BigDecimal getGradient() {
        return gradient;
    }

    /**
     * Used to calculate resistance due to friction. In range: 0 - 0.0127, resolution: 5x10^-5
     * @return
     */
    public BigDecimal getCoefficientRollingResistance() {
        return coefficientRollingResistance;
    }
}
