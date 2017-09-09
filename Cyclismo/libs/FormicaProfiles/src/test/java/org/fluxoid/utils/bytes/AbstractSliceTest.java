package org.fluxoid.utils.bytes;

import org.junit.Before;

public abstract class AbstractSliceTest {

    protected byte [] data;

    @Before
    public void initData() {
        data = new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef};
    }
}
