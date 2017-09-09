package org.fluxoid.utils.bytes;


import org.cowboycoders.ant.utils.IntUtils;

/**
 * Bizarre, but true?
 */
public class NonStandardOps {

    private NonStandardOps() {}

    public static int get_F0FF(byte[] data, int offset){
        int lsb = (0xF0 & data[offset]) >>> 4;
        int msb = (0xff & data[offset +1]) << 4;
        return lsb | msb;
    }

    public static void put_F0FF(byte[] data, int offset, int val) {
        int max = IntUtils.maxUnsigned(12);
        if (val > max) {
            throw new IllegalArgumentException("max: " + max + ", you gave: " + val);
        }
        int lsbLowNibble = 0xf & data[offset];
        int lsbHighNibble = ((val & 0xf) << 4);
        data[offset] = (byte) (lsbHighNibble | lsbLowNibble);
        data[offset + 1] = (byte) ((0xff0 & val) >> 4);
    }
}
