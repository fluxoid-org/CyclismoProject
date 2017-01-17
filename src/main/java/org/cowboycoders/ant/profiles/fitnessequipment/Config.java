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

    public BigDecimal getBicycleWeight() {
        return bicycleWeight;
    }

    public BigDecimal getBicycleWheelDiameter() {
        return bicycleWheelDiameter;
    }

    public BigDecimal getGearRatio() {
        return gearRatio;
    }

    public BigDecimal getUserWeight() {
        return userWeight;
    }



}
