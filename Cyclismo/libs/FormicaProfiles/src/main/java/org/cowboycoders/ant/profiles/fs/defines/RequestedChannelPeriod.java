package org.cowboycoders.ant.profiles.fs.defines;

import org.cowboycoders.ant.utils.IntUtils;

public enum RequestedChannelPeriod {
    // keep order
    _65535,
    _32768,
    _16384,
    _8192,
    _4096;

    private static final RequestedChannelPeriod[] ordinalMapping = RequestedChannelPeriod.values();

    public static RequestedChannelPeriod from(byte b) {
        int actual = b & 0x7;
        if (actual >= 0 && actual < ordinalMapping.length) {
            return ordinalMapping[actual];
        }
        return _4096;
    }

    public byte encode(byte original) {
        return (byte) IntUtils.setMaskedBits(0xff & original, 0x7, this.ordinal());
    }


}
