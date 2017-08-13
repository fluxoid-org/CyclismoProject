package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.HasCalories;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

/**
 * Created by fluxoid on 08/02/17.
 */
public class CalorieBurntUpdate extends TaggedTelemetryEvent implements HasCalories {

    private final long caloriesBurnt;

    public CalorieBurntUpdate(Object tag, long caloriesBurnt) {
        super(tag);
        this.caloriesBurnt = caloriesBurnt;
    }

    /**
     * @return in kcal
     */
    @Override
    public long getCaloriesBurnt() {
        return caloriesBurnt;
    }
}
