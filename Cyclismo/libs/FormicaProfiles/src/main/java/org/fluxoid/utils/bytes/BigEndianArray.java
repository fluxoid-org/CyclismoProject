package org.fluxoid.utils.bytes;

public class BigEndianArray extends AbstractByteArray {


    public BigEndianArray(byte[] data) {
        super(data);
    }

    @Override
    public int unsignedToInt(int offset, int len) {
        int res = 0;
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            res += (data.get(offset - n + len -1) & 0xff) << shift;
        }

        return res;
    }

    @Override
    public void put(int offset, int len, int val) {
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            data.put(offset + len -1 - n, (byte) ((val >>> shift) & 0xff));
        }

    }
}
