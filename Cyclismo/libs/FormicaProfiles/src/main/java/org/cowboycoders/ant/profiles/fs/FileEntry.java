package org.cowboycoders.ant.profiles.fs;

import org.cowboycoders.ant.profiles.fs.defines.FSConstants;
import org.cowboycoders.ant.profiles.fs.defines.FileAttribute;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.logging.Logger;

public class FileEntry {

    private static Logger logger = Logger.getLogger(FileEntry.class.getSimpleName());

    // 3 byte id, lowest byte seems to be fit fileType and next two are some unique id (implemented with counter),
    // but may not be true for all datatypes (limited info to go on)
    private final int id;
    private final int dataType; // e.g fit file which is 128
    private final int index; // index in directory
    private final int customFlags; // specific to data type
    private final EnumSet<FileAttribute> attr;
    private final int timeStamp;
    private final int length;


    private final boolean timeStampValid;

    public FileEntry(DirectoryHeader header, ByteBuffer buf) {
        if (buf.remaining() != header.getEntryLength()) {
            throw new IllegalArgumentException("expecting " + header.getEntryLength() + " bytes");
        }
        LittleEndianArray view = new LittleEndianArray(buf);
        index = view.unsignedToInt(0,2);
        dataType = view.unsignedToInt(2,1);
        id = view.unsignedToInt(3,3);
        customFlags = view.unsignedToInt(6,1);
        attr = FileAttribute.from(buf.get(7));
        length = view.unsignedToInt(8,4);
        timeStamp = view.unsignedToInt(12,4);
        if (timeStamp > FSConstants.TIMESTAMP_MAX) {
            timeStampValid = false;
        } else {
            timeStampValid = true;
        }


        buf.position(buf.position() + header.getEntryLength());
    }

    public int getId() {
        return id;
    }

    public int getDataType() {
        return dataType;
    }

    public int getIndex() {
        return index;
    }

    public int getCustomFlags() {
        return customFlags;
    }

    public EnumSet<FileAttribute> getAttributes() {
        return attr;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public int getLength() {
        return length;
    }

    public boolean isTimeStampValid() {
        return timeStampValid;
    }

    public static class FileEntryBuilder {
        private int id;
        private int dataType; // e.g fit file which is 128
        private int index; // index in directory
        private int customFlags; // specific to data type
        private EnumSet<FileAttribute> attributes = EnumSet.noneOf(FileAttribute.class);
        private int timeStamp = (int) FSConstants.TIMESTAMP_MAX;
        private int length;

        public int getId() {
            return id;
        }

        public FileEntryBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public int getDataType() {
            return dataType;
        }

        public FileEntryBuilder setDataType(int dataType) {
            this.dataType = dataType;
            return this;
        }

        public int getIndex() {
            return index;
        }

        public FileEntryBuilder setIndex(int index) {
            this.index = index;
            return this;
        }

        public int getCustomFlags() {
            return customFlags;
        }

        public FileEntryBuilder setCustomFlags(int customFlags) {
            this.customFlags = customFlags;
            return this;
        }

        public EnumSet<FileAttribute> getAttributes() {
            return attributes;
        }

        public FileEntryBuilder setAttributes(EnumSet<FileAttribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public FileEntryBuilder addAttributes(FileAttribute ...attribute) {
            Collection c = Arrays.asList(attribute);
            attributes.addAll(c);
            return this;
        }

        public int getTimeStamp() {
            return timeStamp;
        }

        public FileEntryBuilder setTimeStamp(int timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public int getLength() {
            return length;
        }

        public FileEntryBuilder setLength(int length) {
            this.length = length;
            return this;
        }
    }
}
