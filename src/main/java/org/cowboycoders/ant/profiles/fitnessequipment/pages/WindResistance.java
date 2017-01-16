package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 16/01/17.
 */
public class WindResistance implements AntPage {

    private final BigDecimal DEFAULT_WIND_COEFF = new BigDecimal(0.51);
    private final BigDecimal DEFAULT_DRAFTING_FACTOR = new BigDecimal(1.00);
    private final int WIND_COEFFICENT_OFFSET = 6;
    private final int WIND_SPEED_OFFSET = 7;
    private final int DRAFTING_OFFSET = 8;
    private final BigDecimal windResitanceCoeff;
    private final int windSpeed;
    private final BigDecimal draftingFactor;

    public WindResistance(byte [] packet) {
        int rawCoeff = BitManipulation.UnsignedNumFrom1LeByte(packet[WIND_COEFFICENT_OFFSET]);
        if (rawCoeff != BitManipulation.UNSIGNED_INT8_MAX) {
            windResitanceCoeff = new BigDecimal(rawCoeff).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        } else {
            windResitanceCoeff = DEFAULT_WIND_COEFF;
        }
        int windSpeedRaw = BitManipulation.UnsignedNumFrom1LeByte(packet[WIND_SPEED_OFFSET]);
        if (windSpeedRaw != BitManipulation.UNSIGNED_INT8_MAX) {
            windSpeed = windSpeedRaw - 127;
        } else {
            windSpeed = 0;
        }
        int draftingRaw = BitManipulation.UnsignedNumFrom1LeByte(packet[DRAFTING_OFFSET]);
        if (draftingRaw != BitManipulation.UNSIGNED_INT8_MAX) {
            draftingFactor = new BigDecimal(draftingRaw).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

        } else {
            draftingFactor = DEFAULT_DRAFTING_FACTOR;
        }

    }

    /**
     * (Frontal Surface Area) *  (Drag Coefficient) * (Air Density) in kg/m. Range: 0 - 1.86kg/m, resolution: 0.01
     */
    public BigDecimal getWindResitanceCoefficent() {
        return windResitanceCoeff;
    }

    /**
     * Head wind (+) / Tail wind (-) in range: -127 to 127, resolution: 1 km/h
     */
    public int getWindSpeed() {
        return windSpeed;
    }

    /**
     * Scales the wind resistance to simulate drafting. In range: 0.00 to 1.00, resolution: 0.01.
     */
    public BigDecimal getDraftingFactor() {
        return draftingFactor;
    }
}
