package org.cowboycoders.ant.profiles.common.utils;

/**
 * Created by fluxoid on 02/01/17.
 */
public class CounterUtils {
    public static long calcDelta(int rollover, int o, int n) {
        if (o > n) {
            return n - o + 1 + rollover;
        } else {
            return n - o;
        }
    }
}
