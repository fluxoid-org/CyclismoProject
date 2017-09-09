package org.fluxoid.utils.bytes;

import org.cowboycoders.ant.utils.IntUtils;

/**
 * Big Endian bit level slice
**/
public class BitLevelSlice extends AbstractBitLevelSlice {

    public BitLevelSlice(byte [] data, int offset, int length) {
        super(data, offset, length);
    }

    @Override
    protected int setBits(int wholeElement,int mask, int value) {
        return IntUtils.setMaskedBits(wholeElement,mask, value);
    }

    @Override
    protected int getBits(int wholeElement, int mask) {
        return IntUtils.getFromMask(wholeElement, mask);
    }


}
