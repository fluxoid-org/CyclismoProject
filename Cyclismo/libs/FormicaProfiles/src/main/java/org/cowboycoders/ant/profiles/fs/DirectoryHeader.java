package org.cowboycoders.ant.profiles.fs;

import org.cowboycoders.ant.profiles.fs.defines.FSConstants;
import org.cowboycoders.ant.profiles.fs.defines.TimeFormat;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.nio.ByteBuffer;
import java.util.GregorianCalendar;

public class DirectoryHeader {

    public static final int HEADER_LENGTH = 16;
    private final int systemTimestamp;

    // length of file entry in bytes
    private final int entryLength;

    // when was directory last modified (seconds)
    private final int modifiedTimestamp;

    // ant-fs versioning
    private final int majorVersion;
    private final int minorVersion;

    // how to interpret timestamps
    private final TimeFormat timeFormat;

    private DirectoryHeader(ByteBuffer buf) {
        if(buf.remaining() < HEADER_LENGTH) {
            throw new IllegalArgumentException("malformed directory: length not correct");
        }
        ByteBuffer slice = buf.slice();
        slice.limit(HEADER_LENGTH);
        LittleEndianArray view = new LittleEndianArray(slice);
        majorVersion = view.getPartialByte(0,0b1111_0000);
        minorVersion = view.getPartialByte(0, 0b0000_1111);
        if (majorVersion == 0 && minorVersion == 1) {
            entryLength = view.unsignedToInt(1,1);
            if (entryLength != 16) {
                throw new IllegalArgumentException("entry length expected to be 16");
            }
            timeFormat = TimeFormat.from(slice.get(2));
            systemTimestamp = view.unsignedToInt(8,4);
            modifiedTimestamp = view.unsignedToInt(12,4);

        } else {
            throw new IllegalArgumentException("do not know how to decode version: " + majorVersion + "." + minorVersion);
        }
        buf.position(buf.position() + HEADER_LENGTH);
    }

    public int getSystemTimestamp() {
        return systemTimestamp;
    }

    public int getEntryLength() {
        return entryLength;
    }

    public int getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public static class DirectoryHeaderBuilder {

        private int systemTimestamp;

        // length of file entry in bytes
        private int entryLength = 16;

        // when was directory last modified (seconds)
        private int modifiedTimestamp;

        // ant-fs versioning
        private final int majorVersion = 0;
        private final int minorVersion = 1;

        // how to interpret timestamps
        private TimeFormat timeFormat = TimeFormat.Auto;

        public void encode(ByteBuffer buf) {
            ByteBuffer src = ByteBuffer.allocate(8);
            LittleEndianArray view = new LittleEndianArray(src);
            view.putPartialByte(0,0b1111_0000, majorVersion);
            view.putPartialByte(0,0b0000_1111, minorVersion);
            view.putUnsigned(1,1,entryLength);
            view.putUnsigned(2,1, timeFormat.ordinal());
            view.putUnsigned(8,4, systemTimestamp);
            view.putUnsigned(12,4, modifiedTimestamp);
            buf.put(src);
        }

        public long getSystemTimestamp() {
            return systemTimestamp;
        }

        public DirectoryHeaderBuilder setSystemTimestamp(GregorianCalendar cal) {
            this.systemTimestamp = (int) ((cal.getTimeInMillis() - FSConstants.TIME_OFFSET) / 1000);
            return this;
        }

        public int getEntryLength() {
            return entryLength;
        }

        public DirectoryHeaderBuilder setEntryLength(int entryLength) {
            this.entryLength = entryLength;
            return this;
        }

        public long getModifiedTimestamp() {
            return modifiedTimestamp;
        }

        public DirectoryHeaderBuilder setModifiedTimestamp(GregorianCalendar cal) {
            this.modifiedTimestamp = (int) ((cal.getTimeInMillis() - FSConstants.TIME_OFFSET) / 1000);
            return this;
        }

        public int getMajorVersion() {
            return majorVersion;
        }

        public int getMinorVersion() {
            return minorVersion;
        }

        public TimeFormat getTimeFormat() {
            return timeFormat;
        }

        public DirectoryHeaderBuilder setTimeFormat(TimeFormat timeFormat) {
            this.timeFormat = timeFormat;
            return this;
        }
    }


}
