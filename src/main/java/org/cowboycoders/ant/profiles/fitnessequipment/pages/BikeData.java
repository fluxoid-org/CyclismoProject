package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Page 21
 * Created by fluxoid on 02/01/17.
 */
public class BikeData extends CommonPageData {

    public static final int CADENCE_OFFSET = 5;

    /**
     * @return in rpm
     */
    public int getCadence() {
        return cadence;
    }

    /**
     *
     * @return in watts
     */
    public int getPower() {
        return power;
    }

    private final int cadence;
    private final int power;

    public BikeData(byte[] data) {
        super(data);
        final int cadenceRaw = UnsignedNumFrom1LeByte(data[CADENCE_OFFSET]);
        if (cadenceRaw != UNSIGNED_INT8_MAX) {
            cadence = cadenceRaw;
        } else {
            cadence = 0;
        }
        final int powerRaw = UnsignedNumFrom2LeBytes(data, 6);
        if (powerRaw != UNSIGNED_INT16_MAX) {
            power = powerRaw;
        } else {
            power = 0;
        }
    }
}
