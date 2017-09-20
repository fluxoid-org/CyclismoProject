package org.fluxoid.utils;

import org.cowboycoders.ant.utils.IntUtils;

public class ValidationUtils {
    private ValidationUtils() {}

    public static void validate(int min, int max, int val) {
        if (val < min || val > max) {
            throw new IllegalArgumentException("valid range: " + min + " <= x <= " + max + ", you gave: " + val);
        }
    }

    public static void validateUnsigned(int max, int val) {
        // would like to use Integer.compareUnsigned, but don't due to android api level concerns
        if (IntUtils.compareUnsigned(val, max) > 0) {
            throw new IllegalArgumentException("valid range: x <= " + (0xffffffffL & max) + ", you gave: " +
                    (0xffffffffL & val));
        }
    }

}
