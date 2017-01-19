package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.common.utils.CounterUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Created by fluxoid on 02/01/17.
 */
public class MetabolicData extends CommonPageData {

    private static final int META_OFFSET = 7;
    private static final int HAS_CALORIES_MASK = 0x1;
    private static final int CALORIES_OFFSET = 6;
    private static final int INSTANT_METABOLIC_EQUIVALENTS_OFFSET = 2;
    private static final int INSTANT_CALORIE_OFFSET = 4;

    private final boolean caloriesAvailable;
    private final Integer calorieCounter;

    /**
     *
     * @return whether or not @see getCalorieCounter is available
     */
    public boolean isCummulativeCaloriesAvailable() {
        return caloriesAvailable;
    }

    /**
     * calories burnt counter
     */
    public Integer getCalorieCounter() {
        return calorieCounter;
    }

    /**
     * @return  Rate of energy expenditure in METs, 0.01 MET resolution
     */
    public BigDecimal getInstantMetabolicEquivalent() {
        return instantMetabolicEquivalents;
    }

    /**
     *
     * @return kcal/hr burnt, with 0.1 kcla/hr resolution
     */
    public BigDecimal getInstantCalorieBurn() {
        return instantCalorieBurn;
    }

    private final BigDecimal instantMetabolicEquivalents;
    private final BigDecimal instantCalorieBurn;

    public MetabolicData(byte[] packet) {
        super(packet);
        caloriesAvailable = booleanFromU8(packet[META_OFFSET], HAS_CALORIES_MASK);
        if (caloriesAvailable) {
            calorieCounter = UnsignedNumFrom1LeByte(packet[CALORIES_OFFSET]);
        } else {
            calorieCounter = null;
        }

        final int instantMetaRaw = UnsignedNumFrom2LeBytes(packet, INSTANT_METABOLIC_EQUIVALENTS_OFFSET);
        if (instantMetaRaw != UNSIGNED_INT16_MAX) {
            instantMetabolicEquivalents = new BigDecimal(instantMetaRaw).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        } else {
            instantMetabolicEquivalents = null;
        }

        final int instantCalorieRaw = UnsignedNumFrom2LeBytes(packet, INSTANT_CALORIE_OFFSET);
        if (instantCalorieRaw != UNSIGNED_INT16_MAX) {
            instantCalorieBurn = new BigDecimal(instantCalorieRaw).divide(new BigDecimal(10), 1, RoundingMode.HALF_UP);
        } else {
            instantCalorieBurn = null;
        }


    }

    public long getCalorieDelta(MetabolicData old) {
        if (old == null) {
            return calorieCounter;
        }
        return CounterUtils.calcDelta(UNSIGNED_INT8_MAX, old.calorieCounter, calorieCounter);
    }
}
