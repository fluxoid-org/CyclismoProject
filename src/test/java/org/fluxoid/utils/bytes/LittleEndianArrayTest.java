package org.fluxoid.utils.bytes;
import org.fluxoid.utils.Format;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LittleEndianArrayTest {

    @Test
    public void unsignedEncodeDecode()  {
        final int offset = 0;
        final int len = 4;
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianArray slice = new LittleEndianArray(test1);
        assertTrue(slice.unsignedToInt(offset, len) == 0xffffffff);


        slice.put(offset, len, Integer.MAX_VALUE);
        assertTrue(slice.unsignedToInt(offset, len) == Integer.MAX_VALUE);
        assertTrue(slice.unsignedToLong(offset, len) == Integer.MAX_VALUE);



    }

    @Test
    public void unsignedEncodeDecode2()  {
        final int offset = 1;
        final int len = 2;
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianArray slice = new LittleEndianArray(test1);
        slice.put(offset, len, 0xabcd);
        assertEquals(0xabcd, slice.unsignedToInt(offset, len));
    }

    @Test
    public void signedEncodeDecode()  {
        final int offset = 0;
        final int len = 4;
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianArray slice = new LittleEndianArray(test1);

        slice.put(offset, len, Integer.MIN_VALUE);
        System.out.println(Format.bytesToString(test1));
        assertEquals(Integer.MIN_VALUE, slice.signedToInt(offset, len));

        slice.put(offset, len, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, slice.signedToInt(offset, len));

    }

    @Test
    public void signedEncodeDecodeUnderMaxLen()  {
        final int offset = 0;
        final int len = 3;
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianArray slice = new LittleEndianArray(test1);

        slice.put(offset, len, -1234);
        assertEquals(-1234, slice.signedToInt(offset, len));
        slice.put(offset, len, 0x5eadbe);
        assertEquals(0x5eadbe, slice.signedToInt(offset, len));

    }

    @Test(expected = IllegalArgumentException.class)
    public void overSignedLimit()  {
        final int offset = 0;
        final int len = 3;
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianArray slice = new LittleEndianArray(test1);
        slice.putSigned(offset, len, 0xdeadbe);

    }

    @Test
    public void atSignedLimit()  {
        final int offset = 0;
        final int len = 3;
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianArray slice = new LittleEndianArray(test1);
        slice.putSigned(offset, len, 8388607);
        slice.putSigned(offset, len, -8388608);
        slice.putSigned(offset, 4, Integer.MAX_VALUE);
        slice.putSigned(offset, 4, Integer.MIN_VALUE);
    }


}
