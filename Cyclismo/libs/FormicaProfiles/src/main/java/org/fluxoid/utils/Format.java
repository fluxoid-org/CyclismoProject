package org.fluxoid.utils;

import java.util.Formatter;

/**
 * Created by fluxoid on 02/07/17.
 */
public class Format {
    public static CharSequence bytesToString(Byte[] arr) {
        Formatter formatter = new Formatter();
        final int len = arr.length * 3;
        for (byte b: arr) {
            formatter.format("%02x:",b);
        }
        // strip last char
        int end = arr.length == 0 ? 0 : len - 1;
        return formatter.toString().substring(0, end);
    }

    public static CharSequence bytesToString(byte[] arr) {
        Formatter formatter = new Formatter();
        final int len = arr.length * 3;
        for (byte b: arr) {
            formatter.format("%02x:",b);
        }
        // strip last char
        int end = arr.length == 0 ? 0 : len - 1;
        return formatter.toString().substring(0, end);
    }

    public static String format(String fmt, Object ... args) {
        try (Formatter formatter = new Formatter()){
            formatter.format(fmt, args);
            return formatter.out().toString();
        }
    }
}
