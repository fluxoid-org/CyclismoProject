package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.fitnessequipment.CommandStatus;
import org.cowboycoders.ant.profiles.fitnessequipment.CommandStatusBuilder;
import org.cowboycoders.ant.profiles.pages.CommonCommandPage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

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
        CommandStatusBuilder builder = new CommandStatusBuilder();
        Defines.CommandId cmd = Defines.CommandId.getValueFromInt(this.getLastCommandPage());
        byte[] responseData = this.getResponseData();
        builder.setLastReceivedCommandId(cmd);
        builder.setLastReceivedSequenceNumber(this.getLastSequenceNumber());
        builder.setStatus(Defines.Status.getValueFromInt(this.getStatus().getIntValue()));
        builder.setRawResponseData(Arrays.copyOf(responseData, responseData.length));
        switch (cmd) {
            case BASIC_RESISTANCE:
                BigDecimal resistance = new BigDecimal(BitManipulation.UnsignedNumFrom1LeByte(responseData[3]))
                        .divide(new BigDecimal(2), 1, RoundingMode.HALF_UP);
                builder.setTotalResistance(resistance);
                break;
            case TARGET_POWER:
                BigDecimal targetPower = new BigDecimal(BitManipulation.UnsignedNumFrom2LeBytes(responseData, 2))
                        .divide(new BigDecimal(4), 2, RoundingMode.HALF_UP);
                builder.setTargetPower(targetPower);
                break;
            case WIND_RESISTANCE:
                int coeff = BitManipulation.UnsignedNumFrom1LeByte(responseData[1]);
                if (coeff != BitManipulation.UNSIGNED_INT8_MAX) {
                    builder.setWindResistanceCoefficient(
                            new BigDecimal(coeff).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                    );
                }
                int windSpeed = BitManipulation.UnsignedNumFrom1LeByte(responseData[1]);
                if (windSpeed != BitManipulation.UNSIGNED_INT8_MAX) {
                    builder.setWindSpeed(windSpeed - 127);
                }
                int draft = BitManipulation.UnsignedNumFrom1LeByte(responseData[3]);
                if (draft != BitManipulation.UNSIGNED_INT8_MAX) {
                    builder.setDraftingFactor(
                            new BigDecimal(draft).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)
                    );
                }
                break;
            case TRACK_RESISTANCE:
                int gradeRaw = BitManipulation.UnsignedNumFrom2LeBytes(responseData, 1);
                if (gradeRaw != BitManipulation.UNSIGNED_INT16_MAX) {
                    builder.setGrade(
                      new BigDecimal(gradeRaw).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                              .subtract(new BigDecimal(200))
                    );
                }
                int rollCoeff = BitManipulation.UnsignedNumFrom1LeByte(responseData[3]);
                if (rollCoeff != BitManipulation.UNSIGNED_INT8_MAX) {
                    builder.setRollingResistanceCoefficient(
                            new BigDecimal(rollCoeff).divide(new BigDecimal(20000), 5, RoundingMode.HALF_UP)
                    );
                }
                break;

        }
        status = builder.createCommandStatus();
    }

}
