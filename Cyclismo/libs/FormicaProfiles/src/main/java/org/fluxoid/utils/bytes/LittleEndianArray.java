package org.fluxoid.utils.bytes;

import org.cowboycoders.ant.utils.IntUtils;

public class LittleEndianArray extends AbstractByteArray {

    public static final byte SIGN_BYTE_MASK = (byte) 0b1000_0000;

    public LittleEndianArray(byte[] data) {
        super(data);
    }

    @Override
    public int unsignedToInt(int offset, int len) {
        assert len <= IntUtils.BYTES;
        int res = 0;
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            res += (data[offset + n] & 0xff) << shift;
        }

        return res;
    }

    public long unsignedToLong(int offset, int len) {
        assert len <= 8;
        int res = 0;
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            res += (data[offset + n] & 0xFFL) << shift;
        }

        return res;
    }


    public int signedToInt(int offset, int len) {
        assert len > 0;
        boolean isNegative = (byte) (data[offset + len -1] & SIGN_BYTE_MASK) == SIGN_BYTE_MASK;
        int ret = unsignedToInt(offset, len);
        if (isNegative) {
            int signExtension = ~(IntUtils.maxSigned(8 * len));
            ret = ret | signExtension;
        }
        return ret;
    }

    @Override
    public void put(int offset, int len, int val) {
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            data[offset + n] = (byte) ((val >>> shift) & 0xff);
        }

    }


    public void put(int offset, int len, long val) {
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            data[offset + n] = (byte) ((val >>> shift) & 0xffL);
        }

    }

}
