package org.fluxoid.utils.bytes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BigEndianBufferTest {

    @Test
    public void oneByte() {
        final int offset = 0;
        final int len = 1;
        byte [] data = new byte[] {(byte)0xff};
        BigEndianArray test = new BigEndianArray(data);
        assertEquals(0xff, test.unsignedToInt(offset, len));

        test.put(offset, len,0xab);
        assertEquals(0xab, test.unsignedToInt(offset, len));

    }

    @Test
    public void twoBytes() {
        final int offset = 0;
        final int len = 2;
        byte [] data = new byte[] {(byte)0xff, (byte) 0xff};
        BigEndianArray test = new BigEndianArray(data);
        assertEquals(0xffff, test.unsignedToInt(offset, len));

        test.put(offset, len, 0xabcd);
        assertEquals(0xabcd, test.unsignedToInt(offset, len));

    }
}
