package org.cowboycoders.ant.profiles;

import org.fluxoid.utils.bytes.NonStandardOps;

/**
 * Created by fluxoid on 29/12/16.
 */
public class BitManipulation
{

    public static final int UNSIGNED_INT16_MAX = 65535;
    public static final int UNSIGNED_INT8_MAX = 255;
    public static final int UNSIGNED_INT12_MAX = 4095;

    public static boolean intToBoolean(int i) {return i !=0;}

    public static boolean booleanFromU8(byte b, int mask) {return intToBoolean(b & mask);}

    public static byte clearMaskedBits(byte b, int mask) {
        if (mask < 0 || mask > 255) {
            throw new IllegalArgumentException("mask out of range");
        }
        return (byte) ((b & 0xff) & (~mask));
    }

    public static void setFlag(boolean flag, byte[] packet, int off, int mask) {
        if (flag) {
            packet[off] |= mask;
        } else {
            packet[off] = clearMaskedBits(packet[off], mask);
        }
    }

}