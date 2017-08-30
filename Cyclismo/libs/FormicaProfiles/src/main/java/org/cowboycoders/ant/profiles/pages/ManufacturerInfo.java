package org.cowboycoders.ant.profiles.pages;

import org.cowboycoders.ant.profiles.BitManipulation;

public class ManufacturerInfo implements AntPage {
    public static final int PAGE_NUMBER = 80;
    private static final int HARDWARE_REV_OFFSET = 3;
    private static final int MANUFACTURER_ID_OFFSET = 4;
    private static final int MODEL_NUMBER_OFFSET = 6;
    private final int revision;
    private final int id;
    private final int model;


    public ManufacturerInfo(byte[] data) {
        this.revision = BitManipulation.UnsignedNumFrom1LeByte(data[HARDWARE_REV_OFFSET]);
        this.id = BitManipulation.UnsignedNumFrom2LeBytes(data, MANUFACTURER_ID_OFFSET);
        this.model = BitManipulation.UnsignedNumFrom2LeBytes(data, MODEL_NUMBER_OFFSET);
    }

    /**
     * @return Hardware revision
     */
    public int getRevision() {
        return revision;
    }

    /**
     * {@see org.cowboycoders.ant.profiles.common.ManufacurerIds}
     * @return identifier of manufacturer
     */
    public int getManufacturerId() {
        return id;
    }

    /**
     * @return distinguishes different models from same manufacturer
     */
    public int getModel() {
        return model;
    }

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }
}
