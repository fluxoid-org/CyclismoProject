package org.fluxoid.utils.bytes;

public class ArraySlice extends AbstractSlice {

    private final BigEndianArray buffer;

    public ArraySlice(byte[] data, int offset, int length) {
        super(data, offset, length);
        this.buffer = new BigEndianArray(data);
    }

    @Override
    public int unsignedToInt() {
        return buffer.unsignedToInt(offset,length);
    }

    @Override
    public void putUnsigned(int val) {
        buffer.put(offset, length, val);
    }
}
