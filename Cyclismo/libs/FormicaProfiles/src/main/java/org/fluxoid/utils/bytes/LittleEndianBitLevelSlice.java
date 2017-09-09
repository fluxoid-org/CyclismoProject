package org.fluxoid.utils.bytes;

import org.cowboycoders.ant.utils.IntUtils;

/**
 * This is for little endian arrays
 *
 * You cannot *partially* straddle multiple bytes with this class. Shown by example:
 *
 * 0xcab becomes 0xab0c in little endian representation; masking three nibbles would produce 0xab0, losing the lower
 * nibble.
 */
public class LittleEndianBitLevelSlice extends AbstractBitLevelSlice {

    public LittleEndianBitLevelSlice(byte [] data, int offset, int length) {
        super(data, offset, length);
    }

    ;

    @Override
    protected int setBits(int wholeElement,int mask, int value) {
        return IntUtils.setMaskedBitsLE(wholeElement,mask, value, length);
    }

    @Override
    protected int getBits(int wholeElement, int mask) {
        return IntUtils.getFromMaskLE(wholeElement, mask, length);
    }


}
