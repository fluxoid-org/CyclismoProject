package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.PutUnsignedNumIn1LeBytes;
import static org.cowboycoders.ant.profiles.BitManipulation.UNSIGNED_INT8_MAX;

/**
 * Created by fluxoid on 16/01/17.
 */
public class WindResistance implements AntPage {

    public static final int PAGE_NUMBER = 50;

    private static final BigDecimal DEFAULT_WIND_COEFF = new BigDecimal(0.51);
    private static final BigDecimal DEFAULT_DRAFTING_FACTOR = new BigDecimal(1.00);
    private static final int WIND_COEFFICIENT_OFFSET = 5;
    private static final int WIND_SPEED_OFFSET = 6;
    private static final int DRAFTING_OFFSET = 7;
    private final BigDecimal windResitanceCoeff;
    private final int windSpeed;
    private final BigDecimal draftingFactor;

    public static class WindResistancePayload {
        private BigDecimal windResitanceCoeff = DEFAULT_WIND_COEFF;
        private int windSpeed;
        private BigDecimal draftingFactor = DEFAULT_DRAFTING_FACTOR;

        public BigDecimal getWindResitanceCoeff() {
            return windResitanceCoeff;
        }

        public WindResistancePayload setWindResistanceCoeff(BigDecimal windResitanceCoeff) {
            if (windResitanceCoeff == null) {
                throw new IllegalArgumentException("windResitanceCoeff cannot be null");
            }
            this.windResitanceCoeff = windResitanceCoeff;
            return this;
        }

        public int getWindSpeed() {
            return windSpeed;
        }

        public WindResistancePayload setWindSpeed(int windSpeed) {
            if (windSpeed < -127 || windSpeed > 127) {
                throw new IllegalArgumentException("wind speed out of range");
            }
            this.windSpeed = windSpeed;
            return this;
        }

        public BigDecimal getDraftingFactor() {
            return draftingFactor;
        }

        public WindResistancePayload setDraftingFactor(BigDecimal draftingFactor) {
            if (draftingFactor == null) {
                throw new IllegalArgumentException("drafting factor cannot be null");
            }
            this.draftingFactor = draftingFactor;
            return this;
        }

        private static BigDecimal setScale(BigDecimal d) {
            return d.setScale(2, RoundingMode.HALF_UP);
        }


        public void encode(final byte[] packet) {
            PutUnsignedNumIn1LeBytes(packet, PAGE_OFFSET, PAGE_NUMBER);
            if (setScale(windResitanceCoeff).equals(setScale(DEFAULT_WIND_COEFF))){
                PutUnsignedNumIn1LeBytes(packet, WIND_COEFFICIENT_OFFSET, UNSIGNED_INT8_MAX);
            } else {
                BigDecimal raw = windResitanceCoeff.multiply(new BigDecimal(100))
                        .setScale(0, RoundingMode.HALF_UP);
                PutUnsignedNumIn1LeBytes(packet, WIND_COEFFICIENT_OFFSET, raw.intValue());
            }
            if (windSpeed == 0) {
                PutUnsignedNumIn1LeBytes(packet, WIND_SPEED_OFFSET, UNSIGNED_INT8_MAX);
            } else {
                PutUnsignedNumIn1LeBytes(packet, WIND_SPEED_OFFSET, windSpeed + 127);
            }
            if (setScale(draftingFactor).equals(setScale(DEFAULT_DRAFTING_FACTOR))) {
                PutUnsignedNumIn1LeBytes(packet, DRAFTING_OFFSET, UNSIGNED_INT8_MAX);
            } else {
                BigDecimal raw = draftingFactor.multiply(new BigDecimal(100))
                        .setScale(0, RoundingMode.HALF_UP);
                PutUnsignedNumIn1LeBytes(packet, DRAFTING_OFFSET, raw.intValue());
            }
        }

        public WindResistance createWindResistance() {
            final byte[] packet = new byte[8];
            this.encode(packet);
            return new WindResistance(packet);
        }
    }

    public WindResistance(byte [] packet) {
        int rawCoeff = BitManipulation.UnsignedNumFrom1LeByte(packet[WIND_COEFFICIENT_OFFSET]);
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
    public BigDecimal getWindResistanceCoefficent() {
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
