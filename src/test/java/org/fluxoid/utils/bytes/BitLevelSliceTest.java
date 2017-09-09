package org.fluxoid.utils.bytes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitLevelSliceTest extends AbstractSliceTest {

    @Test
    public void basic() {
        BitLevelSlice slice = new BitLevelSlice(data, 1, 11);
        int value = 1234;
        slice.putUnsigned(value);
        //System.out.println(Format.bytesToString(data));
        assertEquals(value, slice.unsignedToInt());
    }
}
