package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Page 2
 * Created by fluxoid on 02/01/17.
 */
public class CalibrationProgress  implements AntPage {

    private static final int OFFSET_IN_PROGRESS_MASK = 0x40;
    private static final int SPINDOWN_IN_PROGRESS_MASK = 0x80;
    private static final int CONDITION_OFFSET = 3;
    private static final int FLAG_OFFSET = 2;
    private static final int TEMP_OFFSET = 4;
    private static final int SPEED_OFFSET = 5;
    private static final int SPINDOWN_OFFSET = 7;

    private final Defines.SpeedCondition speedState;
    private final Defines.TemperatureCondition tempState;
    private final boolean offsetPending;
    private final boolean spinDownPending;

    public Defines.SpeedCondition getSpeedState() {
        return speedState;
    }

    public Defines.TemperatureCondition getTempState() {
        return tempState;
    }

    public boolean isOffsetPending() {
        return offsetPending;
    }

    public boolean isSpinDownPending() {
        return spinDownPending;
    }

    /**
     *  Range: 0ms - 65534ms
     * @return ideal spinDowntime
     */
    public Integer getTargetSpinDownTime() {
        return targetSpinDownTime;
    }

    /**
     *  0m/s - 65.534m/s
     * @return speed that must be reached before letting wheel coast
     */
    public BigDecimal getTargetSpeed() {
        return targetSpeed;
    }

    /**
     * -25C - +100C, 0.5C resolution
     */
    public BigDecimal getTemp() {
        return temp;
    }

    private final Integer targetSpinDownTime;
    private final BigDecimal targetSpeed;
    private final BigDecimal temp;



    public CalibrationProgress(byte[] data) {
        offsetPending = booleanFromU8(data[FLAG_OFFSET], OFFSET_IN_PROGRESS_MASK);
        spinDownPending = booleanFromU8(data[FLAG_OFFSET], SPINDOWN_IN_PROGRESS_MASK);
        speedState = Defines.SpeedCondition.getValueFromInt(UnsignedNumFromUpper2BitsOfUpperNibble(data[CONDITION_OFFSET]));
        tempState = Defines.TemperatureCondition.getValueFromInt(UnsignedNumFromLower2BitsOfUpperNibble(data[CONDITION_OFFSET]));
        final int tempRaw = UnsignedNumFrom1LeByte(data[TEMP_OFFSET]);
        if (tempRaw != UNSIGNED_INT8_MAX) {
            temp = new BigDecimal(tempRaw).divide(new BigDecimal(2),1, BigDecimal.ROUND_HALF_UP).subtract(new BigDecimal(25));
        } else {
            temp = null;
        }
        final int speedRaw = UnsignedNumFrom2LeBytes(data, SPEED_OFFSET);
        if (speedRaw != UNSIGNED_INT16_MAX) {
            targetSpeed = new BigDecimal(speedRaw).divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP);
        } else {
            targetSpeed = null;
        }
        final int spinDownRaw = UnsignedNumFrom2LeBytes(data, SPINDOWN_OFFSET);
        if (spinDownRaw != UNSIGNED_INT16_MAX) {
            targetSpinDownTime = spinDownRaw;
        } else {
            targetSpinDownTime = null;
        }



    }


}
