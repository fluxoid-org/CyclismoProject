package org.cowboycoders.ant.profiles.common.events;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 16/01/17.
 */
public class SpeedUpdate {

    /**
     * @return km/h
     */
    public BigDecimal getSpeed() {
        return speed;
    }

    public SpeedUpdate(BigDecimal speed) {
        this.speed = speed;
    }

    private final BigDecimal speed;

}
