package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

/**
 * Created by fluxoid on 08/02/17.
 */
public class CalorieBurntUpdate implements TelemetryEvent {

    private final long caloriesBurnt;

    public CalorieBurntUpdate(long caloriesBurnt) {
        this.caloriesBurnt = caloriesBurnt;
    }

    /**
     * @return in kcal
     */
    public long getCaloriesBurnt() {
        return caloriesBurnt;
    }
}
