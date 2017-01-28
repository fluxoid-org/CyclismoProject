package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Page 17
 * Created by fluxoid on 02/01/17.
 */
public class GeneralSettings extends CommonPageData implements AntPage {

    public static final int PAGE_NUMBER = 17;

    private static final int CYCLE_LENGTH_OFFSET = 3;
    private static final int INCLINE_OFFSET = 4;
    private static final int RESISTANCE_OFFSET = 6;

    private final BigDecimal cycleLength;
    private final BigDecimal incline;
    private final Integer resistance;


    public static class GeneralSettingsPayload extends CommonPagePayload {
        private BigDecimal cycleLength;
        private BigDecimal incline;
        private Integer resistance;

        public BigDecimal getCycleLength() {
            return cycleLength;
        }

        public GeneralSettingsPayload setCycleLength(BigDecimal cycleLength) {
            this.cycleLength = cycleLength;
            return this;
        }

        public BigDecimal getIncline() {
            return incline;
        }

        public GeneralSettingsPayload setIncline(BigDecimal incline) {
            this.incline = incline;
            return this;
        }

        public Integer getResistance() {
            return resistance;
        }

        public GeneralSettingsPayload setResistance(Integer resistance) {
            this.resistance = resistance;
            return this;
        }

        @Override
        public GeneralSettingsPayload setLapFlag(boolean lapflag) {
            return (GeneralSettingsPayload) super.setLapFlag(lapflag);
        }

        @Override
        public GeneralSettingsPayload setState(Defines.EquipmentState state) {
            return (GeneralSettingsPayload) super.setState(state);
        }

        public void encode(final byte[] packet) {
            super.encode(packet);
            PutUnsignedNumIn1LeBytes(packet, PAGE_OFFSET, PAGE_NUMBER);
            if (cycleLength == null) {
                PutUnsignedNumIn1LeBytes(packet, CYCLE_LENGTH_OFFSET, UNSIGNED_INT8_MAX);
            } else {
                BigDecimal raw = cycleLength.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
                PutUnsignedNumIn1LeBytes(packet, CYCLE_LENGTH_OFFSET, raw.byteValue());
            }
            if (incline == null) {
                PutUnsignedNumIn2LeBytes(packet, INCLINE_OFFSET, Short.MAX_VALUE);
            } else {
                BigDecimal raw = incline.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
                PutSignedNumIn2LeBytes(packet, INCLINE_OFFSET, raw.intValue());
            }
            if (resistance == null) {
                PutUnsignedNumIn1LeBytes(packet, RESISTANCE_OFFSET, UNSIGNED_INT8_MAX);
            } else {
                PutUnsignedNumIn1LeBytes(packet, RESISTANCE_OFFSET, resistance);
            }
        }
    }

    /**
     * length of complete "cycle" for the equipment being used, eg:
     *
     * elliptical : stride length
     * step machine : step height
     * rower : stroke length
     *
     * @return cycle length in m, 0.01m resolution
     */
    public BigDecimal getCycleLength() {
        return cycleLength;
    }

    /**
     * @return treadmill specific incline -100% to 100%, resolution 0.01%, if available, otherwise null.
     */
    public BigDecimal getIncline() {
        return incline;
    }

    /**
     * can be either raw (1-254) or percentage (0-100%) depending on piece of equipment
     */
    public Integer getResistance() {
        return resistance;
    }

    public GeneralSettings(byte[] data) {
        super(data);
        final int cycleLengthRaw = UnsignedNumFrom1LeByte(data[CYCLE_LENGTH_OFFSET]);
        if (cycleLengthRaw != UNSIGNED_INT8_MAX) {
            cycleLength = new BigDecimal(cycleLengthRaw).divide(new BigDecimal(100),2, RoundingMode.HALF_UP);
        } else {
            cycleLength = null;
        }
        final int inclineRaw = SignedNumFrom2LeBytes(data, INCLINE_OFFSET);
        if (inclineRaw != Short.MAX_VALUE) {
            incline = new BigDecimal(inclineRaw).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        } else {
            incline = null;
        }
        final int resistanceRaw = UnsignedNumFrom1LeByte(data[RESISTANCE_OFFSET]);
        if (resistanceRaw != UNSIGNED_INT8_MAX) {
            resistance = resistanceRaw;
        } else {
            resistance = null;
        }



    }

}
