package org.fluxoid.utils.bytes;

public abstract class AbstractSlice {
    protected final byte[] data;
    protected final int length;
    protected final int offset;

    public AbstractSlice(byte [] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public abstract int unsignedToInt();

    public abstract void putUnsigned(int val);
}
