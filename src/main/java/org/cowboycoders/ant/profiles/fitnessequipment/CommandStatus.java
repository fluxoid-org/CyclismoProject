package org.cowboycoders.ant.profiles.fitnessequipment;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 17/01/17.
 */
public class CommandStatus {

    private BigDecimal draftingFactor;
    private BigDecimal grade;
    private Defines.CommandId lastReceivedCommandId;
    private int lastReceivedSequenceNumber;
    private byte[] rawResponseData;
    private BigDecimal rollingResistanceCoefficient;
    private Defines.Status status;
    private BigDecimal targetPower;
    private BigDecimal totalResistance;
    private BigDecimal windResistanceCoefficient;
    private Integer windSpeed;

    public BigDecimal getDraftingFactor() {
        return draftingFactor;
    }

    public BigDecimal getGrade() {
        return grade;
    }

    public Defines.CommandId getLastReceivedCommandId() {
        return lastReceivedCommandId;
    }

    public int getLastReceivedSequenceNumber() {
        return lastReceivedSequenceNumber;
    }

    public byte[] getRawResponseData() {
        return rawResponseData;
    }

    public BigDecimal getRollingResistanceCoefficient() {
        return rollingResistanceCoefficient;
    }

    public Defines.Status getStatus() {
        return status;
    }

    public BigDecimal getTargetPower() {
        return targetPower;
    }

    public BigDecimal getTotalResistance() {
        return totalResistance;
    }

    public BigDecimal getWindResistanceCoefficient() {
        return windResistanceCoefficient;
    }

    public Integer getWindSpeed() {
        return windSpeed;
    }

    protected CommandStatus(BigDecimal draftingFactor, BigDecimal grade, Defines.CommandId lastReceivedCommandId, int lastReceivedSequenceNumber, byte[] rawResponseData, BigDecimal rollingResistanceCoefficient, Defines.Status status, BigDecimal targetPower, BigDecimal totalResistance, BigDecimal windResistanceCoefficient, Integer windSpeed) {
        this.draftingFactor = draftingFactor;
        this.grade = grade;
        this.lastReceivedCommandId = lastReceivedCommandId;
        this.lastReceivedSequenceNumber = lastReceivedSequenceNumber;
        this.rawResponseData = rawResponseData;
        this.rollingResistanceCoefficient = rollingResistanceCoefficient;
        this.status = status;
        this.targetPower = targetPower;
        this.totalResistance = totalResistance;
        this.windResistanceCoefficient = windResistanceCoefficient;
        this.windSpeed = windSpeed;
    }




}
