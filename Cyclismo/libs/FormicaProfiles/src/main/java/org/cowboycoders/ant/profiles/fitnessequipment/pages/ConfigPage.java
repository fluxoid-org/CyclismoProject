package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Config;
import org.cowboycoders.ant.profiles.fitnessequipment.ConfigBuilder;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;
import org.fluxoid.utils.bytes.NonStandardOps;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * p55
 * Created by fluxoid on 16/01/17.
 */
public class ConfigPage implements AntPage {

    public static final int PAGE_NUMBER = 55;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    private static final int USER_WEIGHT_OFFSET = 1;
    private static final int BIKE_WEIGHT_OFFSET = 4;
    private static final int WHEEL_DIAMETER_OFFSET = 6;
    private static final int GEAR_RATIO_OFFSET = 7;
    private final Config config;

    public Config getConfig() {
        return config;
    }

    public static class ConfigPayload implements AntPacketEncodable {
        private Config config;

        public Config getConfig() {
            return config;
        }

        public ConfigPayload setConfig(Config config) {
            this.config = config;
            return this;
        }

        public void encode(final byte[] packet) {
            LittleEndianArray viewer = new LittleEndianArray(packet);
            viewer.putUnsigned(PAGE_OFFSET,1,PAGE_NUMBER);
            BigDecimal uw = config.getUserWeight();
            if (uw == null) {
                viewer.putUnsigned(USER_WEIGHT_OFFSET,2,UNSIGNED_INT16_MAX);;
            } else {
                viewer.putUnsigned(USER_WEIGHT_OFFSET,2,
                        uw.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).intValue());

            }
            BigDecimal bw = config.getBicycleWeight();
            if (bw == null) {
                NonStandardOps.put_F0FF(packet, BIKE_WEIGHT_OFFSET, UNSIGNED_INT12_MAX);
            } else {
                NonStandardOps.put_F0FF(packet, BIKE_WEIGHT_OFFSET,
                        bw.multiply(new BigDecimal(20)).setScale(0, RoundingMode.HALF_UP)
                .intValue());
            }
            BigDecimal dia = config.getBicycleWheelDiameter();
            if (dia == null) {
                viewer.putUnsigned(WHEEL_DIAMETER_OFFSET,1,UNSIGNED_INT8_MAX);
            } else {
                viewer.putUnsigned(WHEEL_DIAMETER_OFFSET,1,
                        dia.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP)
                        .intValue());

            }
            BigDecimal ratio = config.getGearRatio();
            if (ratio == null){
                viewer.putUnsigned(GEAR_RATIO_OFFSET, 1, UNSIGNED_INT8_MAX);
            } else {
                BigDecimal raw = ratio.divide(new BigDecimal(0.03), RoundingMode.HALF_UP);
                viewer.putUnsigned(GEAR_RATIO_OFFSET, 1, raw.intValue());
            }


        }
    }

    public ConfigPage(byte[] packet) {
        ConfigBuilder builder = new ConfigBuilder();
        LittleEndianArray viewer = new LittleEndianArray(packet);
        int uWeight = viewer.unsignedToInt(USER_WEIGHT_OFFSET, 2);
        if (uWeight != UNSIGNED_INT16_MAX) {
            builder.setUserWeight(
                    new BigDecimal(uWeight).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
            );
        }
        int bWeight = NonStandardOps.get_F0FF(packet, BIKE_WEIGHT_OFFSET);
        if (bWeight != UNSIGNED_INT12_MAX) {
            builder.setBicycleWeight(
                    new BigDecimal(bWeight).divide(new BigDecimal(20), 2, RoundingMode.HALF_UP)
            );
        }
        int dia = viewer.unsignedToInt(WHEEL_DIAMETER_OFFSET,1);
        if (dia != UNSIGNED_INT8_MAX) {
            builder.setBicycleWheelDiameter(
                    new BigDecimal(dia).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
            );
        }
        int ratio = viewer.unsignedToInt(GEAR_RATIO_OFFSET,1);
        if (ratio != UNSIGNED_INT8_MAX) {
            builder.setGearRatio(
                    new BigDecimal(ratio).multiply(new BigDecimal(0.03))
                    .setScale(2, RoundingMode.HALF_UP)
            );
        }
        config = builder.createConfig();

    }
}
