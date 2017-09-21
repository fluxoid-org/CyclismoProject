package org.cowboycoders.ant.profiles.fs.defines;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum FileAttribute {
    READ    (0b1000_0000),
    WRITE   (0b0100_0000),
    ERASE   (0b0010_0000),
    COMPRESS(0b0001_0000),
    APPEND  (0b0000_1000),
    ENCRYPT (0b0000_0100);

    private int mask;
    FileAttribute(int mask) {
        this.mask = mask;
    }

    public static EnumSet<FileAttribute> from(byte attr) {
        List<FileAttribute> attributes = new ArrayList<>();
        for (FileAttribute candidate: FileAttribute.values()) {
            if ((attr & candidate.mask) != 0) {
                attributes.add(candidate);
            }
        }
        if (attributes.size() > 0) {
            return EnumSet.copyOf(attributes);
        }
        return EnumSet.noneOf(FileAttribute.class);
    }

    public static byte encode(EnumSet<FileAttribute> attrs) {
        byte ret = 0;
        for (FileAttribute attr: attrs) {
            ret |= attr.mask;
        }
        return ret;
    }

    public static void main(String[] args) {
        System.out.printf("%x\n", encode(EnumSet.of(READ)));
        System.out.printf("%x\n", encode(EnumSet.allOf(FileAttribute.class)));
    }
}
