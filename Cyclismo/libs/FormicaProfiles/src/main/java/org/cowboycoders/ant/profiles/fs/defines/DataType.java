package org.cowboycoders.ant.profiles.fs.defines;

/**
 * Some standard data types
 */
public enum DataType implements HasDataTypeId {
    FIT_FILE(0x80),
    BLOOD_PRESSURE(0xe);

    private final int id;

    DataType(int id) {
        this.id = id;
    }

    public int getDataTypeId() {
        return id;
    }
}
