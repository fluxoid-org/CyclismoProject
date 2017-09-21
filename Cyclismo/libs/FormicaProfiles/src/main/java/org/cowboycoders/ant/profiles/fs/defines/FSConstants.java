package org.cowboycoders.ant.profiles.fs.defines;

public class FSConstants {
    // from unix epoch
    public static long TIME_OFFSET = 631065600000L;

    // values greater than this are considered invalid.
    // long is useful for unsigned comparison with int
    public static long TIMESTAMP_MAX = 0xf_ff_ff_ff;
}
