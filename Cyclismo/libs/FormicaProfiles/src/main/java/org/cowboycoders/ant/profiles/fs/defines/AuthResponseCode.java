package org.cowboycoders.ant.profiles.fs.defines;


public enum AuthResponseCode {
    NOT_APPLICABLE,
    ACCEPT,
    REJECT,
    // synthetic
    UNKNOWN;

    private static AuthResponseCode[] values = AuthResponseCode.values();

    public static AuthResponseCode decode(byte b) {
        int index = 0xff & b;
        if (index < values.length) {
            return values[index];
        }
        return UNKNOWN;
    }

    public static <E extends Enum<E>> E[] test(Class<E> clazz) {
        return clazz.getEnumConstants();
    }

    public byte encode() {
        return (byte) this.ordinal();
    }

}
