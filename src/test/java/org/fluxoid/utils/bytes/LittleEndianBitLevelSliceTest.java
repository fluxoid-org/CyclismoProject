package org.fluxoid.utils.bytes;

import org.fluxoid.utils.Format;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LittleEndianBitLevelSliceTest extends AbstractSliceTest {

    @Test
    public void bytesSpannedAligned() {

        for (int i = 1; i<= 8; i++) {
            assertEquals(1, new LittleEndianBitLevelSlice(null,8,i).bytesSpanned);
        }
        for (int i = 9; i<= 16; i++) {
            assertEquals(2, new LittleEndianBitLevelSlice(null,8,i).bytesSpanned);
        }

    }

    @Test
    public void bytesSpannedUnaligned() {
        assertEquals(1, new LittleEndianBitLevelSlice(null,2,6).bytesSpanned);
        assertEquals(2, new LittleEndianBitLevelSlice(null,2,7).bytesSpanned);
        assertEquals(3, new LittleEndianBitLevelSlice(null,3,15).bytesSpanned);
        assertEquals(3, new LittleEndianBitLevelSlice(null,3,21).bytesSpanned);
        assertEquals(4, new LittleEndianBitLevelSlice(null,3,22).bytesSpanned);

        assertEquals(1, new LittleEndianBitLevelSlice(null,28,4).bytesSpanned);
    }

    @Test
    public void genMask() {
        assertEquals(0b1, LittleEndianBitLevelSlice.getMask(0,1));
        assertEquals(0b11, LittleEndianBitLevelSlice.getMask(0,2));
        assertEquals(0b111, LittleEndianBitLevelSlice.getMask(0,3));
        assertEquals(0b1111, LittleEndianBitLevelSlice.getMask(0,4));
        assertEquals(0b11110, LittleEndianBitLevelSlice.getMask(1,4));
        assertEquals(0b111100, LittleEndianBitLevelSlice.getMask(2,4));
    }


    // the mask tests are kind of flaky as they just verify what the
    // current implementation produces.

    @Test
    public void genLeftMask() {
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(null,4,8);
        assertEquals(0b1111_0000_0000_0000, test.getLeftMask());
    }

    @Test
    public void genRightMask() {
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(null,4,8);
        assertEquals(0b0000_0000_0000_1111, test.getRightMask());
    }

    @Test
    public void genRightMaskAligned() {
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(null,4,12);
        assertEquals(0, test.getRightMask() & getMask(12));
    }

    private int getMask(int numBits) {
        return (0b1 << numBits) -1;
    }

    private void testLeftMaskLeftAligned(int len) {
        initData();
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(null,8,len);
        // specific to right aligned case
        int bitsAfterSlice = test.bytesSpanned * 8 -len;

        int overSliceMask = ~((0b1 << bitsAfterSlice) -1);

        int actual = test.getLeftMask();
        //actual = 0xabcd000f;
        //actual = 0b11100000001;
        // ignore bits over slice eg. ignore cd in ab|000|cd
        actual = actual & overSliceMask;
        // ignore bits before slice start, e.g ignore ab in ab|000|cd
        actual = actual & getMask(test.bytesSpanned * 8);
        assertEquals(0, actual);
    }

    @Test
    public void genLeftMaskAligned() {
        testLeftMaskLeftAligned(12);
        testLeftMaskLeftAligned(3);
        testLeftMaskLeftAligned(7);
        testLeftMaskLeftAligned(4);
        testLeftMaskLeftAligned(16);
        testLeftMaskLeftAligned(18);
    }


    @Test
    public void insertUnsignedAtStart() {
        // start
        byte [] expected = new byte[] {(byte) 0xce, (byte) 0xad, (byte) 0xbe, (byte) 0xef};
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(data, 0, 4);
        test.putUnsigned(0xc);
        assertArrayEquals(expected, data);
    }

    @Test
    public void insertUnsignedAtStartMultiNibble() {
        // start
        byte [] expected = new byte[] {(byte) 0xab, (byte) 0xcd, (byte) 0xbe, (byte) 0xef};
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(data, 0, 16);
        test.putUnsigned(0xcdab);
        assertArrayEquals(expected, data);
    }

    @Test
    public void insertUnsignedAtEndMultiNibble() {
        // start
        byte [] expected = new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xab, (byte) 0xcd};
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(data, 16, 16);
        test.putUnsigned(0xcdab);
        assertArrayEquals(expected, data);
    }

    @Test
    public void insertUnsignedInMiddle() {
        // middle
        byte [] expected = new byte[] {(byte) 0xde, (byte) 0xac, (byte) 0xbe, (byte) 0xef};
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(data, 12, 4);
        test.putUnsigned(0xc);
        assertArrayEquals(expected, data);
    }

    @Test
    public void insertUnsignedAtEnd() {
        byte [] expected = new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xec};
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(data, 28, 4);
        test.putUnsigned(0xc);
        assertArrayEquals(expected, data);
    }

    @Test
    public void straddleTwoBytes() {
        byte [] expected = new byte[] {(byte) 0xdc, (byte) 0xcd, (byte) 0xbe, (byte) 0xef};
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(data, 4, 8);
        test.putUnsigned(0xcc);

        assertArrayEquals(expected, data);
    }

    @Test
    public void straddleThreeBytes() {
        byte [] expected = new byte[] {(byte) 0xda, (byte) 0xbc, (byte) 0xde, (byte) 0xef};
        LittleEndianBitLevelSlice test = new LittleEndianBitLevelSlice(data, 4, 16);
        test.putUnsigned(0xcdab);
        assertArrayEquals(expected, data);
    }

    @Test
    public void inOutMiddle() {
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 12, 16);
        slice.putUnsigned(2789);
        System.out.printf("%x\n", 2789);
        System.out.println(Format.bytesToString(data));
        assertEquals(2789, slice.unsignedToInt());
    }

    @Test
    public void test2789() {
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 12, 16);
        byte [] expected = new byte[] {(byte) 0xde, (byte) 0xae, (byte) 0x50, (byte) 0xaf};
        slice.putUnsigned(2789);
        assertArrayEquals(expected, data);
    }
    @Test
    public void inOutStartAligned() {
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 0, 8);
        slice.putUnsigned(123);
        assertEquals(123, slice.unsignedToInt());
    }

    @Test
    public void inOutStart() {
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 0, 16);
        slice.putUnsigned(1234);
        System.out.println(Format.bytesToString(data));
        assertEquals(1234, slice.unsignedToInt());
    }
    @Test
    public void bitLevelOneByte() {
        byte [] data = new byte[] {(byte)0xF0};
        byte expected = (byte) 0b1111_0001;
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 7, 1);
        slice.putUnsigned(1);
        assertEquals(expected, data[0]);
    }

    @Test
    public void bitLevelOneByte2() {
        byte [] data = new byte[] {(byte)0xF0};
        byte expected = (byte) 0b1111_0010;
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 6, 1);
        slice.putUnsigned(1);
        assertEquals(expected, data[0]);
        assertEquals(1, slice.unsignedToInt());
    }

    @Test
    public void bitLevelOneByteHighNibble() {
        byte [] data = new byte[] {(byte)0xFF};
        byte expected = (byte) 0b1011_1111;
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 1, 1);
        slice.putUnsigned(0);
        assertEquals(expected, data[0]);
    }

    @Test
    public void bitLevelGetSet() {
        byte [] data = new byte[] {(byte)0xFF, (byte)0xab};
        byte expected = (byte) 0b1011_1111;
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 1, 1);
        slice.putUnsigned(0);
        assertEquals(expected, data[0]);
        assertEquals((byte)0xab, data[1]);
        assertEquals(0, slice.unsignedToInt());
    }
    @Test
    public void twoBitLevelGetSet() {
        byte [] data = new byte[] {(byte)0xcd, (byte)0xFF, (byte)0xab};
        byte expected = (byte) 0b1001_1111;
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 9, 2);
        slice.putUnsigned(0);
        assertEquals((byte)0xcd, data[0]);
        assertEquals(expected, data[1]);
        assertEquals((byte)0xab, data[2]);
        assertEquals(0, slice.unsignedToInt());
    }

    @Test
    public void threeBitLevelGetSetNonZero() {
        byte [] data = new byte[] {(byte)0xcd, (byte)0xFF, (byte)0xab};
        LittleEndianBitLevelSlice slice = new LittleEndianBitLevelSlice(data, 9, 3);
        slice.putUnsigned(2);
        System.out.println(Format.bytesToString(data));
        assertEquals(2, slice.unsignedToInt());
    }

}
