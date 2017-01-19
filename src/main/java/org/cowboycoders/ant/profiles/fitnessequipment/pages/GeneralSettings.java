package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.UNSIGNED_INT8_MAX;
import static org.cowboycoders.ant.profiles.BitManipulation.UnsignedNumFrom1LeByte;
import static org.cowboycoders.ant.profiles.BitManipulation.UnsignedNumFrom2LeBytes;

/**
 * Page 17
 * Created by fluxoid on 02/01/17.
 */
public class GeneralSettings extends CommonPageData implements AntPage {

    private static final int CYCLE_LENGTH_OFFSET = 3;
    private static final int INCLINE_OFFSET = 4;
    private static final int RESISTANCE_OFFSET = 6;

    private final BigDecimal cycleLength;
    private final BigDecimal incline;
    private final Integer resistance;

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
        final int inclineRaw = UnsignedNumFrom2LeBytes(data, INCLINE_OFFSET);
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
