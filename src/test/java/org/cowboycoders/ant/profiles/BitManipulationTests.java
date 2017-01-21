package org.cowboycoders.ant.profiles;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.cowboycoders.ant.profiles.BitManipulation.PutUnsignedNumInUpper2BitsOfUpperNibble;
import static org.cowboycoders.ant.profiles.BitManipulation.UnsignedNumFromUpper2BitsOfUpperNibble;
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

    @Test
    public void test1() {
        byte [] data = new byte[1];
        final int testVal = 2;
        PutUnsignedNumInUpper2BitsOfUpperNibble(data, 0, testVal);
        assertEquals(testVal, UnsignedNumFromUpper2BitsOfUpperNibble(data[0]));
    }
}
