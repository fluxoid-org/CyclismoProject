package org.cowboycoders.ant.profiles.pages;

import org.fluxoid.utils.bytes.LittleEndianArray;

public class ManufacturerInfo implements AntPage {
    public static final int PAGE_NUMBER = 80;
    private static final int HARDWARE_REV_OFFSET = 3;
    private static final int MANUFACTURER_ID_OFFSET = 4;
    private static final int MODEL_NUMBER_OFFSET = 6;
    private final int revision;
    private final int id;
    private final int model;


    public ManufacturerInfo(byte[] data) {
        LittleEndianArray viewer = new LittleEndianArray(data);
        this.revision = viewer.unsignedToInt(HARDWARE_REV_OFFSET, 1);
        this.id = viewer.unsignedToInt(MANUFACTURER_ID_OFFSET, 2);
        this.model = viewer.unsignedToInt(MODEL_NUMBER_OFFSET, 2);
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
