package org.cowboycoders.ant.profiles.fs.defines;

public enum DownloadResponseCode {
    // 0 : no error , 1 : file doesn't exist, 2 : not readable, 3: not ready, 4: invalid request,
    //5: crc incorrect
    NO_ERROR,
    FILE_DOES_NOT_EXIST,
    ACCESS_DENIED,
    BUSY,
    INVALID_REQUEST,
    CRC_MISMATCH,
    UNKNOWN;

    private static DownloadResponseCode[] values = DownloadResponseCode.values();

    public static DownloadResponseCode decode(byte b) {
        int index = 0xff & b;
        if (index < values.length) {
            return values[index];
        }
        return UNKNOWN;
    }

    public byte encode() {
        return (byte) this.ordinal();
    }
}
