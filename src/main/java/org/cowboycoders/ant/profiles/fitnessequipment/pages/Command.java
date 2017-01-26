package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.fitnessequipment.*;
import org.cowboycoders.ant.profiles.pages.CommonCommandPage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Created by fluxoid on 17/01/17.
 */
public class Command extends CommonCommandPage {

    private final CommandStatus status;

    public CommandStatus getFitnessStatus() {
        return status;
    }

    public Command(byte[] packet) {
        super(packet);
        CommandStatusBuilder commonBuilder = new CommandStatusBuilder();
        Defines.CommandId cmd = Defines.CommandId.getValueFromInt(this.getLastCommandPage());
        byte[] responseData = this.getResponseData();
        commonBuilder.setLastReceivedCommandId(cmd);
        commonBuilder.setLastReceivedSequenceNumber(this.getLastSequenceNumber());
        commonBuilder.setStatus(Defines.Status.getValueFromInt(this.getStatus().getIntValue()));
        commonBuilder.setRawResponseData(Arrays.copyOf(responseData, responseData.length));
        CommandStatus commonStatus = commonBuilder.createCommandStatus();
        switch (cmd) {
            case BASIC_RESISTANCE:
                ResistanceStatusBuilder rsb = new ResistanceStatusBuilder();
                BigDecimal resistance = new BigDecimal(BitManipulation.UnsignedNumFrom1LeByte(responseData[3]))
                        .divide(new BigDecimal(2), 1, RoundingMode.HALF_UP);
                rsb.setTotalResistance(resistance);
                rsb.setStatus(commonStatus);
                status = rsb.createCommandStatus();
                break;
            case TARGET_POWER:
                TargetPowerStatusBuilder tpb = new TargetPowerStatusBuilder();
                BigDecimal targetPower = new BigDecimal(BitManipulation.UnsignedNumFrom2LeBytes(responseData, 2))
                        .divide(new BigDecimal(4), 2, RoundingMode.HALF_UP);
                tpb.setTargetPower(targetPower);
                tpb.setStatus(commonStatus);
                status = tpb.createCommandStatus();
                break;
            case WIND_RESISTANCE:
                WindStatusBuilder builder = new WindStatusBuilder();
                builder.setStatus(commonStatus);
                int coeff = BitManipulation.UnsignedNumFrom1LeByte(responseData[1]);
                if (coeff != UNSIGNED_INT8_MAX) {
                    builder.setWindResistanceCoefficient(
                            new BigDecimal(coeff).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                    );
                }
                int windSpeed = BitManipulation.UnsignedNumFrom1LeByte(responseData[2]);
                if (windSpeed != UNSIGNED_INT8_MAX) {
                    builder.setWindSpeed(windSpeed - 127);
                }
                int draft = BitManipulation.UnsignedNumFrom1LeByte(responseData[3]);
                if (draft != UNSIGNED_INT8_MAX) {
                    builder.setDraftingFactor(
                            new BigDecimal(draft).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)
                    );
                }
                status = builder.createCommandStatus();
                break;
            case TRACK_RESISTANCE:
                TerrainStatusBuilder tsb = new TerrainStatusBuilder();
                tsb.setStatus(commonStatus);
                int gradeRaw = BitManipulation.UnsignedNumFrom2LeBytes(responseData, 1);
                if (gradeRaw != UNSIGNED_INT16_MAX) {
                    tsb.setGrade(
                      new BigDecimal(gradeRaw).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                              .subtract(new BigDecimal(200))
                    );
                }
                int rollCoeff = BitManipulation.UnsignedNumFrom1LeByte(responseData[3]);
                if (rollCoeff != UNSIGNED_INT8_MAX) {
                    tsb.setRollingResistanceCoefficient(
                            new BigDecimal(rollCoeff).divide(new BigDecimal(20000), 5, RoundingMode.HALF_UP)
                    );
                }
                status = tsb.createCommandStatus();
                break;
            default:
                throw new IllegalArgumentException("unexpected packet");

        }
    }

    /**
     * Created by fluxoid on 26/01/17.
     */
    public static class ResistanceStatus extends CommandStatus {
        private BigDecimal totalResistance;

        public BigDecimal getTotalResistance() {
            return totalResistance;
        }

        protected ResistanceStatus(int lastReceivedSequenceNumber, byte[] rawResponseData, Defines.Status status, BigDecimal totalResistance) {
            super(Defines.CommandId.BASIC_RESISTANCE, lastReceivedSequenceNumber, rawResponseData, status);
            this.totalResistance = totalResistance;
        }


    }

    /**
     * Created by fluxoid on 26/01/17.
     */
    public static class ResistanceStatusBuilder {

        private CommandStatus status = new CommandStatusBuilder().createCommandStatus();
        private BigDecimal totalResistance = null;

        public ResistanceStatusBuilder setTotalResistance(BigDecimal totalResistance) {
            this.totalResistance = totalResistance;
            return this;
        }

        public ResistanceStatusBuilder setStatus(CommandStatus status) {
            this.status = status;
            return this;
        }

        public ResistanceStatus createCommandStatus() {
            return new ResistanceStatus(status.getLastReceivedSequenceNumber(),
                    status.getRawResponseData(), status.getStatus(), totalResistance
                    );
        }

        public void encode(byte [] packet) {
            CommandStatusBuilder builder = CommandStatusBuilder.from(status);
            byte [] response = CommonCommandPage.createEmptyResponse();
            if (totalResistance == null) {
               throw new IllegalArgumentException("total resistance must be set");
            } else {
                BigDecimal n = totalResistance.multiply(
                        new BigDecimal(2).setScale(0, RoundingMode.HALF_UP)
                );
                PutUnsignedNumIn1LeBytes(response, 3, n.byteValue());
            }

            builder.setRawResponseData(response);
            builder.setLastReceivedCommandId(Defines.CommandId.BASIC_RESISTANCE);
            builder.createCommandStatus().encode(packet);
        }
    }

    /**
     * Created by fluxoid on 26/01/17.
     */
    public static class TargetPowerStatus extends CommandStatus {

        private BigDecimal targetPower;

        protected TargetPowerStatus(int lastReceivedSequenceNumber, byte[] rawResponseData, Defines.Status status, BigDecimal targetPower) {
            super(Defines.CommandId.TARGET_POWER, lastReceivedSequenceNumber, rawResponseData, status);
            this.targetPower = targetPower;

        }

        public BigDecimal getTargetPower() {
            return targetPower;
        }
    }

    /**
     * Created by fluxoid on 26/01/17.
     */
    public static class TargetPowerStatusBuilder {

        private BigDecimal targetPower = null;
        private CommandStatus status = new CommandStatusBuilder().createCommandStatus();

        public TargetPowerStatusBuilder setTargetPower(BigDecimal targetPower) {
            this.targetPower = targetPower;
            return this;
        }

        public TargetPowerStatusBuilder setStatus(CommandStatus status) {
            this.status = status;
            return this;
        }

        public TargetPowerStatus createCommandStatus() {
            return new TargetPowerStatus(status.getLastReceivedSequenceNumber(),
                    status.getRawResponseData(), status.getStatus(),
                    targetPower);
        }

        public void encode(byte [] packet) {
            CommandStatusBuilder builder = CommandStatusBuilder.from(status);
            byte [] response = CommonCommandPage.createEmptyResponse();
            if (targetPower == null) {
                throw new IllegalArgumentException("targetPower must be set");
            } else {
                BigDecimal n = targetPower.multiply(
                        new BigDecimal(4).setScale(0, RoundingMode.HALF_UP)
                );
                PutUnsignedNumIn2LeBytes(response, 2, n.intValue());
            }

            builder.setRawResponseData(response);
            builder.setLastReceivedCommandId(Defines.CommandId.TARGET_POWER);
            builder.createCommandStatus().encode(packet);
        }

    }

    /**
     * Created by fluxoid on 26/01/17.
     */
    public static class TerrainStatus extends CommandStatus {

        private BigDecimal grade;
        private BigDecimal rollingResistanceCoefficient;

        protected TerrainStatus(int lastReceivedSequenceNumber, byte[] rawResponseData, Defines.Status status,
                                BigDecimal grade, BigDecimal rollingResistanceCoefficient) {
            super(Defines.CommandId.TRACK_RESISTANCE, lastReceivedSequenceNumber, rawResponseData, status);
            this.grade = grade;
            this.rollingResistanceCoefficient = rollingResistanceCoefficient;

        }

        public BigDecimal getGrade() {
            return grade;
        }

        public BigDecimal getRollingResistanceCoefficient() {
            return rollingResistanceCoefficient;
        }
    }

    /**
     * Created by fluxoid on 26/01/17.
     */
    public static class TerrainStatusBuilder {
        private BigDecimal grade = new BigDecimal("0.00");
        private BigDecimal rollingResistanceCoefficient = new BigDecimal("0.004");
        private CommandStatus status = new CommandStatusBuilder().createCommandStatus();

        public TerrainStatusBuilder setGrade(BigDecimal grade) {
            this.grade = grade;
            return this;
        }

        public TerrainStatusBuilder setRollingResistanceCoefficient(BigDecimal rollingResistanceCoefficient) {
            this.rollingResistanceCoefficient = rollingResistanceCoefficient;
            return this;
        }

        public TerrainStatusBuilder setStatus(CommandStatus status) {
            this.status = status;
            return this;
        }

        public TerrainStatus createCommandStatus() {
            return new TerrainStatus(status.getLastReceivedSequenceNumber(),
                    status.getRawResponseData(), status.getStatus(),
                    grade, rollingResistanceCoefficient);
        }

        public void encode(byte [] packet) {
            CommandStatusBuilder builder = CommandStatusBuilder.from(status);
            byte [] response = CommonCommandPage.createEmptyResponse();
            if (grade == null) {
                PutUnsignedNumIn2LeBytes(response, 1 , UNSIGNED_INT16_MAX);
            } else {
                BigDecimal n = grade.add(new BigDecimal(200)).multiply(
                        new BigDecimal(100).setScale(0, RoundingMode.HALF_UP)
                );
                PutUnsignedNumIn2LeBytes(response, 1, n.intValue());
            }
            if (rollingResistanceCoefficient == null) {
                PutUnsignedNumIn1LeBytes(response, 3 , UNSIGNED_INT8_MAX);
            } else {
                BigDecimal n = rollingResistanceCoefficient.multiply(
                        new BigDecimal(20000).setScale(0, RoundingMode.HALF_UP)
                );
                PutUnsignedNumIn1LeBytes(response, 3, n.byteValue());
            }

            builder.setRawResponseData(response);
            builder.setLastReceivedCommandId(Defines.CommandId.TRACK_RESISTANCE);
            builder.createCommandStatus().encode(packet);
        }
    }

    /**
     * Created by fluxoid on 26/01/17.
     */
    public static class WindStatus extends CommandStatus {

        private BigDecimal windResistanceCoefficient;
        private Integer windSpeed;
        private BigDecimal draftingFactor;

        protected WindStatus(int lastReceivedSequenceNumber, byte[] rawResponseData, Defines.Status status,
                             BigDecimal windResistanceCoefficient, Integer windSpeed, BigDecimal draftingFactor) {
            super(Defines.CommandId.WIND_RESISTANCE, lastReceivedSequenceNumber, rawResponseData, status);
            this.windResistanceCoefficient = windResistanceCoefficient;
            this.windSpeed = windSpeed;
            this.draftingFactor = draftingFactor;

        }

        public BigDecimal getWindResistanceCoefficient() {
            return windResistanceCoefficient;
        }

        public Integer getWindSpeed() {
            return windSpeed;
        }

        public BigDecimal getDraftingFactor() {
            return draftingFactor;
        }
    }


    /**
     * Created by fluxoid on 26/01/17.
     */
    public static class WindStatusBuilder {
        private BigDecimal draftingFactor = new BigDecimal("1.00");
        private BigDecimal windResistanceCoefficient = new BigDecimal("0.51");
        private Integer windSpeed = 0;
        private CommandStatus status = new CommandStatusBuilder().createCommandStatus();

        public WindStatusBuilder setDraftingFactor(BigDecimal draftingFactor) {
            this.draftingFactor = draftingFactor;
            return this;
        }

        public WindStatusBuilder setWindResistanceCoefficient(BigDecimal windResistanceCoefficient) {
            this.windResistanceCoefficient = windResistanceCoefficient;
            return this;
        }

        public WindStatusBuilder setWindSpeed(Integer windSpeed) {
            this.windSpeed = windSpeed;
            return this;
        }

        public WindStatusBuilder setStatus(CommandStatus status) {
            this.status = status;
            return this;
        }

        public WindStatus createCommandStatus() {
            return new WindStatus(status.getLastReceivedSequenceNumber(),
                    status.getRawResponseData(), status.getStatus(),
                    windResistanceCoefficient, windSpeed, draftingFactor);
        }

        public void encode(byte [] packet) {
            CommandStatusBuilder builder = CommandStatusBuilder.from(status);
            byte [] response = CommonCommandPage.createEmptyResponse();
            if (windResistanceCoefficient == null) {
                PutUnsignedNumIn1LeBytes(response, 1 , UNSIGNED_INT8_MAX);
            } else {
                BigDecimal n = windResistanceCoefficient.multiply(
                        new BigDecimal(100).setScale(0, RoundingMode.HALF_UP)
                );
                PutUnsignedNumIn1LeBytes(response, 1, n.byteValue());
            }
            if (windSpeed == null) {
                PutUnsignedNumIn1LeBytes(response, 2 , UNSIGNED_INT8_MAX);
            } else {
                // for -127 to 127 km/h
                PutUnsignedNumIn1LeBytes(response, 2, windSpeed + 127);
            }
            if (draftingFactor == null || draftingFactor.setScale(2).equals(new BigDecimal(1).setScale(2))) {
                // if draftingFactor = 1.00 send (byte) 255
                PutUnsignedNumIn1LeBytes(response, 3 , UNSIGNED_INT8_MAX);
            } else {
                BigDecimal n = draftingFactor.multiply(
                        new BigDecimal(100).setScale(0, RoundingMode.HALF_UP));
                PutUnsignedNumIn1LeBytes(response, 3, n.byteValue());
            }

            builder.setRawResponseData(response);
            builder.setLastReceivedCommandId(Defines.CommandId.WIND_RESISTANCE);
            builder.createCommandStatus().encode(packet);
        }

    }
}
