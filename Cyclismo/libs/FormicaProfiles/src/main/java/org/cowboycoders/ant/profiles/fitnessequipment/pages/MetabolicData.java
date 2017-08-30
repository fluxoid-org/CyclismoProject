package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.common.decode.interfaces.CalorieCountDecodable;
import org.cowboycoders.ant.profiles.common.utils.CounterUtils;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.MathCompat;
import org.fluxoid.utils.RollOverVal;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Created by fluxoid on 02/01/17.
 */
public class MetabolicData extends CommonPageData implements AntPage, CalorieCountDecodable {

    public static final int PAGE_NUMBER = 18;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    private static final int META_OFFSET = 7;
    private static final int HAS_CALORIES_MASK = 0x1;
    private static final int CALORIES_OFFSET = 6;
    private static final int INSTANT_METABOLIC_EQUIVALENTS_OFFSET = 2;
    private static final int INSTANT_CALORIE_OFFSET = 4;

    private final boolean caloriesAvailable;
    private final Integer calorieCounter;

    public static class MetabolicDataPayload extends CommonPagePayload implements AntPacketEncodable {
        private RollOverVal calorieCounter = new RollOverVal(UNSIGNED_INT8_MAX);
        private BigDecimal instantMetabolicEquivalents;
        private BigDecimal instantCalorieBurn;

        public boolean isCaloriesAvailable() {
            return calorieCounter != null;
        }

        public Integer getCalorieCounter() {
            return MathCompat.toIntExact(calorieCounter.getValue());
        }

        public MetabolicDataPayload setCalorieCounter(Integer calorieCounter) {
            this.calorieCounter.setValue(calorieCounter);
            return this;
        }

        public BigDecimal getInstantMetabolicEquivalents() {
            return instantMetabolicEquivalents;
        }

        public MetabolicDataPayload setInstantMetabolicEquivalents(BigDecimal instantMetabolicEquivalents) {
            this.instantMetabolicEquivalents = instantMetabolicEquivalents;
            return this;
        }

        public BigDecimal getInstantCalorieBurn() {
            return instantCalorieBurn;
        }

        public MetabolicDataPayload setInstantCalorieBurn(BigDecimal instantCalorieBurn) {
            this.instantCalorieBurn = instantCalorieBurn;
            return this;
        }

        @Override
        public MetabolicDataPayload setLapFlag(boolean lapflag) {
            return (MetabolicDataPayload) super.setLapFlag(lapflag);
        }

        @Override
        public MetabolicDataPayload setState(Defines.EquipmentState state) {
            return (MetabolicDataPayload) super.setState(state);
        }

        public void encode(byte[] packet) {
            super.encode(packet);
            PutUnsignedNumIn1LeBytes(packet, PAGE_OFFSET, PAGE_NUMBER);
            if (isCaloriesAvailable() && calorieCounter == null) {
                throw new IllegalArgumentException("must set calorie counter");
            }
            if (isCaloriesAvailable()) {
                packet[META_OFFSET] |= HAS_CALORIES_MASK;
                PutUnsignedNumIn1LeBytes(packet, CALORIES_OFFSET, MathCompat.toIntExact(calorieCounter.get()));
            } else {
                packet[META_OFFSET] = clearMaskedBits(packet[META_OFFSET], HAS_CALORIES_MASK);
            }
            if (instantMetabolicEquivalents == null) {
                PutUnsignedNumIn2LeBytes(packet, INSTANT_METABOLIC_EQUIVALENTS_OFFSET, UNSIGNED_INT16_MAX);
            } else {
                BigDecimal raw = instantMetabolicEquivalents.multiply(new BigDecimal(100))
                        .setScale(0, RoundingMode.HALF_UP);
                PutUnsignedNumIn2LeBytes(packet, INSTANT_METABOLIC_EQUIVALENTS_OFFSET, raw.intValue());
            }
            if (instantCalorieBurn == null) {
                PutUnsignedNumIn2LeBytes(packet, INSTANT_CALORIE_OFFSET, UNSIGNED_INT16_MAX);
            } else {
                BigDecimal raw = instantCalorieBurn.multiply(new BigDecimal(10))
                        .setScale(0, RoundingMode.HALF_UP);
                PutUnsignedNumIn2LeBytes(packet, INSTANT_CALORIE_OFFSET, raw.intValue());
            }
        }
    }

    /**
     *
     * @return whether or not @see getCalorieCounter is available
     */
    @Override
    public boolean isCummulativeCaloriesAvailable() {
        return caloriesAvailable;
    }

    /**
     * calories burnt counter
     */
    @Override
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
     * @return kcal/hr burnt, with 0.1 kcal/hr resolution
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

    @Override
    public long getCalorieDelta(CalorieCountDecodable old) {
        if (old == null) {
            return calorieCounter;
        }
        return CounterUtils.calcDelta(UNSIGNED_INT8_MAX, old.getCalorieCounter(), calorieCounter);
    }
}
