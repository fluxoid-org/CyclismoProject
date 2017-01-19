package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Config;
import org.cowboycoders.ant.profiles.fitnessequipment.ConfigBuilder;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * p55
 * Created by fluxoid on 16/01/17.
 */
public class ConfigPage implements AntPage {

    private static final int USER_WEIGHT_OFFSET = 1;
    private static final int BIKE_WEIGHT_OFFSET = 4;
    private static final int WHEEL_DIAMETER_OFFSET = 6;
    private static final int GEAR_RATIO_OFFSET = 7;
    private final Config config;

    public Config getConfig() {
        return config;
    }

    public ConfigPage(byte[] packet) {
        ConfigBuilder builder = new ConfigBuilder();
        int uWeight = UnsignedNumFrom2LeBytes(packet, USER_WEIGHT_OFFSET);
        if (uWeight != UNSIGNED_INT16_MAX) {
            builder.setUserWeight(
                    new BigDecimal(uWeight).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
            );
        }
        int bWeight = UnsignedNumFromUpper1And1HalfLeBytes(packet, BIKE_WEIGHT_OFFSET);
        if (bWeight != UNSIGNED_INT12_MAX) {
            builder.setBicycleWeight(
                    new BigDecimal(bWeight).divide(new BigDecimal(20), 2, RoundingMode.HALF_UP)
            );
        }
        int dia = UnsignedNumFrom1LeByte(packet[WHEEL_DIAMETER_OFFSET]);
        if (dia != UNSIGNED_INT8_MAX) {
            builder.setBicycleWheelDiameter(
                    new BigDecimal(dia).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
            );
        }
        int ratio = UnsignedNumFrom1LeByte(packet[GEAR_RATIO_OFFSET]);
        if (ratio != UNSIGNED_INT8_MAX) {
            builder.setGearRatio(
                    new BigDecimal(ratio).multiply(new BigDecimal(0.03))
                    .setScale(2, RoundingMode.HALF_UP)
            );
        }
        config = builder.createConfig();

    }
}
