package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Page 17
 * Created by fluxoid on 02/01/17.
 */
public class GeneralSettings extends CommonPageData implements AntPage {

    public static final int PAGE_NUMBER = 17;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

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
            LittleEndianArray viewer = new LittleEndianArray(packet);
            viewer.putUnsigned(PAGE_OFFSET, 1, PAGE_NUMBER);
            if (cycleLength == null) {
                viewer.putUnsigned(CYCLE_LENGTH_OFFSET, 1, UNSIGNED_INT8_MAX);
            } else {
                BigDecimal raw = cycleLength.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
                viewer.putUnsigned(CYCLE_LENGTH_OFFSET, 1, raw.intValue());
            }
            if (incline == null) {
                viewer.putUnsigned(INCLINE_OFFSET,2, (int) Short.MAX_VALUE);
            } else {
                BigDecimal raw = incline.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
                viewer.putSigned(INCLINE_OFFSET, 2, raw.intValue());
            }
            if (resistance == null) {
                viewer.putUnsigned(RESISTANCE_OFFSET, 1, UNSIGNED_INT8_MAX);
            } else {
                viewer.putUnsigned(RESISTANCE_OFFSET, 1, (int) resistance);
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
        LittleEndianArray viewer = new LittleEndianArray(data);
        final int cycleLengthRaw = viewer.unsignedToInt(CYCLE_LENGTH_OFFSET, 1);
        if (cycleLengthRaw != UNSIGNED_INT8_MAX) {
            cycleLength = new BigDecimal(cycleLengthRaw).divide(new BigDecimal(100),2, RoundingMode.HALF_UP);
        } else {
            cycleLength = null;
        }
        final int inclineRaw = viewer.signedToInt(INCLINE_OFFSET, 2);
        if (inclineRaw != Short.MAX_VALUE) {
            incline = new BigDecimal(inclineRaw).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        } else {
            incline = null;
        }
        final int resistanceRaw = viewer.unsignedToInt(RESISTANCE_OFFSET, 1);
        if (resistanceRaw != UNSIGNED_INT8_MAX) {
            resistance = resistanceRaw;
        } else {
            resistance = null;
        }



    }

}
