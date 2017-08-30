package org.cowboycoders.ant.profiles.fitnessequipment;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 16/01/17.
 */
public class Config {
    private BigDecimal bicycleWeight;
    private BigDecimal bicycleWheelDiameter;
    private BigDecimal gearRatio;
    private BigDecimal userWeight;

    protected Config(BigDecimal bicycleWeight, BigDecimal bicycleWheelDiameter, BigDecimal gearRatio, BigDecimal userWeight) {
        this.bicycleWeight = bicycleWeight;
        this.bicycleWheelDiameter = bicycleWheelDiameter;
        this.gearRatio = gearRatio;
        this.userWeight = userWeight;
    }

    /**
     *
     * @return in kg
     */
    public BigDecimal getBicycleWeight() {
        return bicycleWeight;
    }

    /**
     *
     * @return in m
     */
    public BigDecimal getBicycleWheelDiameter() {
        return bicycleWheelDiameter;
    }

    /**
     * Front to back gear ratio
     * @return in m
     */
    public BigDecimal getGearRatio() {
        return gearRatio;
    }

    /**
     *
     * @return in kg
     */
    public BigDecimal getUserWeight() {
        return userWeight;
    }



}
