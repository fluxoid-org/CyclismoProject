package org.cowboycoders.ant.profiles;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Created by fluxoid on 21/01/17.
 */
public class BitManipulationTests {

    @Test
    public void testClearBits() {
        byte t1 = (byte) 255;
        assertEquals(0xfe, 0xff & BitManipulation.clearMaskedBits(t1, 1));
        assertEquals(0, 0xff & BitManipulation.clearMaskedBits(t1, 0xff));
        assertEquals(0x0f, 0xff & BitManipulation.clearMaskedBits(t1, 0xf0));
        assertEquals(0xf0, 0xff & BitManipulation.clearMaskedBits(t1, 0x0f));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testClearBitsOutofRange() {
        thrown.expect(IllegalArgumentException.class);
        BitManipulation.clearMaskedBits((byte) 0xff, 256);

    }

    @Test
    public void testClearBitsOutofRangeLower() {
        thrown.expect(IllegalArgumentException.class);
        BitManipulation.clearMaskedBits((byte) 0xff, -1);
    }


}
