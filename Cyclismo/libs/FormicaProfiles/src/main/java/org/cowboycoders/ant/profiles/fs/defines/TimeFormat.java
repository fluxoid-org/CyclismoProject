package org.cowboycoders.ant.profiles.fs.defines;

/**
 * Ant-fs time format specifier
 */
public enum TimeFormat {
    System,
    Auto,
    Date,
    Unknown;

    private static TimeFormat [] values = TimeFormat.values();

    public static TimeFormat from(byte raw) {
        if (raw >= 0 && raw < values.length) {
            return values[raw];
        }
        return Unknown;
    }
}
