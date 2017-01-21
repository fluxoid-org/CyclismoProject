package org.cowboycoders.ant.profiles;

/**
 * Created by fluxoid on 29/12/16.
 */
public class BitManipulation
{

    public static final int UNSIGNED_INT16_MAX = 65535;
    public static final int UNSIGNED_INT8_MAX = 255;
    public static final int UNSIGNED_INT12_MAX = 4095;

    public static void PutSignedNumIn4LeBytes(final byte[] array, final int offset, final long toSqueeze) {
        if (toSqueeze > Integer.MAX_VALUE || toSqueeze < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Value outside the bounds of unsigned 4 byte integer");
        }
        array[offset] = (byte)(0xFFL & toSqueeze);
        array[offset + 1] = (byte)(0xFFL & toSqueeze >> 8);
        array[offset + 2] = (byte)(0xFFL & toSqueeze >> 16);
        array[offset + 3] = (byte)(0xFFL & toSqueeze >> 24);
    }

    public static void PutUnsignedNumIn1LeBytes(final byte[] array, final int offset, final int n2) {
        if (n2 > 255 || n2 < 0) {
            throw new IllegalArgumentException("Value outside the bounds of unsigned 1 byte integer");
        }
        array[offset] = (byte)(n2 & 0xFF);
    }

    public static void PutUnsignedNumIn2BeBytes(final byte[] array, final int offset, final int n2) {
        if (n2 > 65535 || n2 < 0) {
            throw new IllegalArgumentException("Value outside the bounds of unsigned 2 byte integer");
        }
        array[offset + 1] = (byte)(n2 & 0xFF);
        array[offset] = (byte)(0xFF & n2 >> 8);
    }

    public static void PutUnsignedNumIn2LeBytes(final byte[] array, final int offset, final int n2) {
        if (n2 > 65535 || n2 < 0) {
            throw new IllegalArgumentException("Value outside the bounds of unsigned 2 byte integer");
        }
        array[offset] = (byte)(n2 & 0xFF);
        array[offset + 1] = (byte)(0xFF & n2 >> 8);
    }

    public static void PutUnsignedNumIn4LeBytes(final byte[] array, final int offset, final long n2) {
        if (n2 > 4294967295L || n2 < 0L) {
            throw new IllegalArgumentException("Value outside the bounds of unsigned 4 byte integer");
        }
        array[offset] = (byte)(0xFFL & n2);
        array[offset + 1] = (byte)(0xFFL & n2 >> 8);
        array[offset + 2] = (byte)(0xFFL & n2 >> 16);
        array[offset + 3] = (byte)(0xFFL & n2 >> 24);
    }

    public static void PutUnsignedNumInUpper1And1HalfLeBytes(final byte[] array, final int offset, final int n2) {
        if (n2 > 4095 || n2 < 0) {
            throw new IllegalArgumentException("Value outside the bounds of unsigned 1.5 byte integer");
        }
        array[offset] = (byte)((0xF & array[offset]) + (0xF0 & n2 << 4));
        array[offset + 1] = (byte)(0xFF & n2 >> 4);
    }

    public static short SignedNumFrom2LeBytes(final byte[] array, final int n) {
        return (short)(0xFFFF & UnsignedNumFrom2LeBytes(array, n));
    }

    public static int SignedNumFrom4LeBytes(final byte[] array, final int n) {
        return array[n + 3] << 24 | (0xFF & array[n]) | (0xFF00 & array[n + 1] << 8) | (0xFF0000 & array[n + 2] << 16);
    }

    public static int UnsignedNumFrom1LeByte(final byte b) {
        return b & 0xFF;
    }

    public static int UnsignedNumFrom2BeBytes(final byte[] array, final int n) {
        return 0xFFFF & (0xFF & array[n + 1]) + (0xFF00 & array[n] << 8);
    }

    public static int UnsignedNumFrom2LeBytes(final byte[] array, final int n) {
        return 0xFFFF & (0xFF & array[n]) + (0xFF00 & array[n + 1] << 8);
    }

    public static int UnsignedNumFrom3BeBytes(final byte[] array, final int n) {
        return 0xFFFFFF & (0xFF & array[n + 2]) + (0xFF00 & array[n + 1] << 8) + (0xFF0000 & array[n] << 16);
    }

    public static int UnsignedNumFrom3LeBytes(final byte[] array, final int n) {
        return 0xFFFFFF & (0xFF & array[n]) + (0xFF00 & array[n + 1] << 8) + (0xFF0000 & array[n + 2] << 16);
    }

    public static long UnsignedNumFrom4LeBytes(final byte[] array, final int n) {
        return 0xFFFFFFFFL & (0xFFL & array[n]) + (0xFF00 & array[n + 1] << 8) + (0xFF0000 & array[n + 2] << 16) + (0xFF000000 & array[n + 3] << 24);
    }

    public static long UnsignedNumFrom7LeBytes(final byte[] array, final int n) {
        return 0xFFFFFFFFFFFFFFL & (0xFFL & array[n]) + (0xFF00 & array[n + 1] << 8) + (0xFF0000 & array[n + 2] << 16) + (0xFF000000L & array[n + 3] << 24) + (0xFF00000000L & array[n + 4] << 32) + (0xFF0000000000L & array[n + 5] << 40) + (0xFF000000000000L & array[n + 6] << 48);
    }

    public static int UnsignedNumFromLower2BitsOfLowerNibble(final byte b) {
        return b & 0x3;
    }

    public static int UnsignedNumFromLower2BitsOfUpperNibble(final byte b) {
        return 0x3 & b >> 4;
    }

    public static int UnsignedNumFromLower4Bits(final byte b) {
        return b & 0xF;
    }

    public static int UnsignedNumFromUpper1And1HalfLeBytes(final byte[] array, final int n) {
        return 0xFFF & (0xF & array[n] >>> 4) + (0xFF0 & array[n + 1] << 4);
    }

    public static int UnsignedNumFromUpper2BitsOfLowerNibble(final byte b) {
        return 0x3 & b >> 2;
    }

    public static int UnsignedNumFromUpper2BitsOfUpperNibble(final byte b) {
        return 0x3 & b >> 6;
    }

    public static void PutUnsignedNumInUpper2BitsOfUpperNibble(final byte[] array, final int offset, int n) {
        if (n < 0 || n > 0x3) { throw new IllegalArgumentException("number out of range");};
        array[offset] |= n << 6;
    }

    public static void PutUnsignedNumInLower2BitsOfUpperNibble(final byte[] array, final int offset, int n) {
        if (n < 0 || n > 0x3) { throw new IllegalArgumentException("number out of range");};
        array[offset] |= n << 4;
    }

    public static int UnsignedNumFromUpper4Bits(final byte b) {
        return 0xF & b >> 4;
    }

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