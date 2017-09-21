package org.fluxoid.utils.bytes;

import org.cowboycoders.ant.utils.IntUtils;

import java.nio.ByteBuffer;

public abstract class AbstractByteArray {

    protected final ByteBuffer data;

    public AbstractByteArray(byte[] data) {
        this(ByteBuffer.wrap(data));
    }

    public AbstractByteArray(ByteBuffer slice) {
        if (slice == null) {
            throw new IllegalArgumentException("slice cannot be null");
        }
        this.data = slice;
    }

    public abstract int unsignedToInt(int offset, int len);

    public abstract void put(int offset, int len, int val);

    public void putPartialByte(int offset, int mask, int val) {
        final int max = IntUtils.maxValueThatFits(mask);
        if ((mask & 0xff_ff_ff_00) != 0) {
            throw new IllegalArgumentException("you are masking beyond the byte limit");
        }
        if (val > max) {
            throw new IllegalArgumentException("value cannot be more than " + max + ", you gave: " + val);
        }
        int newVal = IntUtils.setMaskedBits(0xff & data.get(offset),mask, val);
        data.put(offset, (byte) newVal);
    }

    public int getPartialByte(int offset, int mask) {
        return IntUtils.getFromMask(data.get(offset) & 0xff, mask);
    }

    public void putSigned(int offset, int len, int val) {
        final int max = IntUtils.maxSigned(len * 8);
        final int min = -max -1;
        if (val > 0 && val > max) {
            throw new IllegalArgumentException("max: " + max + ", you gave: " + val);
        }
        if (val < 0 && val < min) {
            throw new IllegalArgumentException("min: " + min + ", you gave: " + val);
        }
        put(offset,len,val);
    }

    public void putUnsigned(int offset, int len, int val) {
        final int max = IntUtils.maxUnsigned(len * 8);
        // we would like to use Integer.compareUnsigned, but for android api level purposes:
        if (Integer.numberOfLeadingZeros(val) < Integer.numberOfLeadingZeros(max)) {
            throw new IllegalArgumentException("max: " + (0xffffffffL & max) + ", you gave: " + (0xffffffffL & val));
        }
        put(offset,len,val);
    }

}
