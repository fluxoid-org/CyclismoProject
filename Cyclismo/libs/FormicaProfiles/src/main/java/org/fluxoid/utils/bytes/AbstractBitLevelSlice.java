package org.fluxoid.utils.bytes;

import org.cowboycoders.ant.utils.IntUtils;

public abstract class AbstractBitLevelSlice {
    protected final BigEndianArray slice;
    final protected int bytesSpanned;
    private final int bitsOnLeft;
    protected int length;
    protected int offsetBytes;

    public AbstractBitLevelSlice(byte [] data, int offset, int length) {
        bitsOnLeft = offset % 8;
        // ceil((length + bitsOnLeft) / 8)
        bytesSpanned = (length + bitsOnLeft + 7) / 8;
        offsetBytes = offset / 8;
        slice = new BigEndianArray(data);
        this.length = length;
    }

    protected static int getMask(int offset, int numBits) {
        int result = (0b1 << numBits) -1;
        return result << offset;
    }

    protected int getLeftMask() {

        if (bitsOnLeft == 0) {
            // no bits to mask on the left
            return 0;
        }
        int off = (bytesSpanned * 8) - bitsOnLeft;

        return getMask(off, bitsOnLeft);
    }

    protected int getRightMask() {

        int len = bytesSpanned * 8 - length - bitsOnLeft;

        if (len > 0) {
            // there are bits to mask on the right
            return getMask(0, len);
        }

        // there are no bits to mask on the right
        return 0;
    }

    public void putUnsigned(int val) {
        long max = (0b1L << length) -1;
        if (IntUtils.unsignedIntToLong(val) > max) {
            throw new IllegalArgumentException("val can't be larger than: " + max);
        }
        int mask = combineMasks();
        //System.out.printf("invert: %x\n", getRightMask());
        int old = slice.unsignedToInt(offsetBytes, bytesSpanned);
        //System.out.printf("old: %x\n", old);
        int newVal = setBits(old,mask,val);
        //System.out.printf("new: %x\n", newVal);
        slice.put(offsetBytes, bytesSpanned, newVal);
    }

    protected abstract int setBits(int wholeElement, int mask, int value);

    protected abstract int getBits(int wholeElement, int mask);

    private int combineMasks() {
        int invert = (getLeftMask() | getRightMask());
        //System.out.printf("mask: %x\n", getRightMask());
        invert = ~invert;
        invert = invert & getMask(0, bytesSpanned * 8);
        return invert;
    }

    public int unsignedToInt() {
        //System.out.println(align);

        int mask = combineMasks();
        int unmasked = slice.unsignedToInt(offsetBytes, bytesSpanned);
        return getBits(unmasked, mask);
    }
}
