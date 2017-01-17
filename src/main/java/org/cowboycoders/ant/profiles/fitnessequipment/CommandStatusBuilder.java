package org.cowboycoders.ant.profiles.fitnessequipment;

import java.math.BigDecimal;

public class CommandStatusBuilder {
    private BigDecimal draftingFactor = new BigDecimal("1.00");
    private BigDecimal grade = new BigDecimal("0.00");
    private Defines.CommandId lastReceivedCommandId = Defines.CommandId.UNRECOGNIZED;
    private int lastReceivedSequenceNumber = -1;
    private byte[] rawResponseData = new byte [0];
    private BigDecimal rollingResistanceCoefficient = new BigDecimal("0.004");
    private Defines.Status status = Defines.Status.UNINITIALIZED;
    private BigDecimal targetPower = null;
    private BigDecimal totalResistance = null;
    private BigDecimal windResistanceCoefficient = new BigDecimal("0.51");
    private Integer windSpeed = 0;

    public CommandStatusBuilder setDraftingFactor(BigDecimal draftingFactor) {
        this.draftingFactor = draftingFactor;
        return this;
    }

    public CommandStatusBuilder setGrade(BigDecimal grade) {
        this.grade = grade;
        return this;
    }

    public CommandStatusBuilder setLastReceivedCommandId(Defines.CommandId lastReceivedCommandId) {
        this.lastReceivedCommandId = lastReceivedCommandId;
        return this;
    }

    public CommandStatusBuilder setLastReceivedSequenceNumber(int lastReceivedSequenceNumber) {
        this.lastReceivedSequenceNumber = lastReceivedSequenceNumber;
        return this;
    }

    public CommandStatusBuilder setRawResponseData(byte[] rawResponseData) {
        this.rawResponseData = rawResponseData;
        return this;
    }

    public CommandStatusBuilder setRollingResistanceCoefficient(BigDecimal rollingResistanceCoefficient) {
        this.rollingResistanceCoefficient = rollingResistanceCoefficient;
        return this;
    }

    public CommandStatusBuilder setStatus(Defines.Status status) {
        this.status = status;
        return this;
    }

    public CommandStatusBuilder setTargetPower(BigDecimal targetPower) {
        this.targetPower = targetPower;
        return this;
    }

    public CommandStatusBuilder setTotalResistance(BigDecimal totalResistance) {
        this.totalResistance = totalResistance;
        return this;
    }

    public CommandStatusBuilder setWindResistanceCoefficient(BigDecimal windResistanceCoefficient) {
        this.windResistanceCoefficient = windResistanceCoefficient;
        return this;
    }

    public CommandStatusBuilder setWindSpeed(Integer windSpeed) {
        this.windSpeed = windSpeed;
        return this;
    }

    public CommandStatus createCommandStatus() {
        return new CommandStatus(draftingFactor, grade, lastReceivedCommandId, lastReceivedSequenceNumber, rawResponseData, rollingResistanceCoefficient, status, targetPower, totalResistance, windResistanceCoefficient, windSpeed);
    }
}