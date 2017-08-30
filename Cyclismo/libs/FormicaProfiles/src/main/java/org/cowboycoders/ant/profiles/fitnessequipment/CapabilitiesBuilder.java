package org.cowboycoders.ant.profiles.fitnessequipment;

public class CapabilitiesBuilder {
    private boolean basicResistanceModeSupport = false;
    private Integer maximumResistance = null;
    private boolean simulationModeSupport = false;
    private boolean targetPowerModeSupport = false;

    public CapabilitiesBuilder setBasicResistanceModeSupport(boolean basicResistanceModeSupport) {
        this.basicResistanceModeSupport = basicResistanceModeSupport;
        return this;
    }

    public CapabilitiesBuilder setMaximumResistance(Integer maximumResistance) {
        this.maximumResistance = maximumResistance;
        return this;
    }

    public CapabilitiesBuilder setSimulationModeSupport(boolean simulationModeSupport) {
        this.simulationModeSupport = simulationModeSupport;
        return this;
    }

    public CapabilitiesBuilder setTargetPowerModeSupport(boolean targetPowerModeSupport) {
        this.targetPowerModeSupport = targetPowerModeSupport;
        return this;
    }

    public Capabilities createCapabilities() {
        return new Capabilities(basicResistanceModeSupport, maximumResistance, simulationModeSupport, targetPowerModeSupport);
    }
}