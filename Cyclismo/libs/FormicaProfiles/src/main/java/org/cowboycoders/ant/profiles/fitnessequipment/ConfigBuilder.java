package org.cowboycoders.ant.profiles.fitnessequipment;

import java.math.BigDecimal;

public class ConfigBuilder {
    private BigDecimal bicycleWeight = null;
    private BigDecimal bicycleWheelDiameter = null;
    private BigDecimal gearRatio = null;
    private BigDecimal userWeight = null;

    public ConfigBuilder setBicycleWeight(BigDecimal bicycleWeight) {
        this.bicycleWeight = bicycleWeight;
        return this;
    }

    public ConfigBuilder setBicycleWheelDiameter(BigDecimal bicycleWheelDiameter) {
        this.bicycleWheelDiameter = bicycleWheelDiameter;
        return this;
    }

    public ConfigBuilder setGearRatio(BigDecimal gearRatio) {
        this.gearRatio = gearRatio;
        return this;
    }

    public ConfigBuilder setUserWeight(BigDecimal userWeight) {
        this.userWeight = userWeight;
        return this;
    }

    public Config createConfig() {
        return new Config(bicycleWeight, bicycleWheelDiameter, gearRatio, userWeight);
    }
}