package org.cowboycoders.ant.profiles.fs;

import org.cowboycoders.ant.profiles.fs.defines.FileAttribute;
import org.fluxoid.utils.Format;
import org.fluxoid.utils.bytes.LittleEndianArray;
import org.fluxoid.utils.crc.Crc16Utils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.zip.CRC32;

import static org.junit.Assert.assertEquals;

public class DirectoryTest {

    @Test
    public void encodeDecode() {
        DirectoryHeader.DirectoryHeaderBuilder header = new DirectoryHeader.DirectoryHeaderBuilder()
                .setModifiedTimestamp(new GregorianCalendar())
                .setSystemTimestamp(new GregorianCalendar());

        FileEntry soleEntry = new FileEntry.FileEntryBuilder()
                .addAttributes(FileAttribute.READ)
                .setIndex(0)
                .setLength(8)
                .setId(1)
                .setTimeStamp((int) (System.currentTimeMillis() / 1000))
                .setDataType(128) // fit file
                .create();
        Directory dir = new Directory.DirectoryBuilder()
                .setHeader(header)
                .addFile(soleEntry)
                .create();

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        dir.encode(os);
        byte [] data = os.toByteArray();

        ByteBuffer buf = ByteBuffer.wrap(data);

        System.out.println(Format.bytesToString(data));

        Directory decode = new Directory(buf);
        assertEquals(1, decode.getFiles().size());

        FileEntry decodedEntry = decode.getFiles().get(0);
        assertEquals(soleEntry.getAttributes(), decodedEntry.getAttributes());
        assertEquals(soleEntry.getCustomFlags(), decodedEntry.getCustomFlags());
        assertEquals(soleEntry.getDataType(), decodedEntry.getDataType());
        assertEquals(soleEntry.getTimeStamp(), decodedEntry.getTimeStamp());
        assertEquals(soleEntry.getIndex(), decodedEntry.getIndex());
        assertEquals(soleEntry.getId(), decodedEntry.getId());
        assertEquals(soleEntry.getLength(), decodedEntry.getLength());

    }


    @Test
    public void crc() {
        final int expected = 58117;
        byte [] data = new byte[58];
        LittleEndianArray view = new LittleEndianArray(data);
        data[0] = 67;
        data[2] = 3; // guess
        data[8] = 68;
        data[9] = (byte) 0x89; // download response codetS
        view.put(12,4,32); // length of data
        view.put(16,4,0); // offset of data
        view.put(20,4,32); // file size;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mkDir().encode(os);
        int crc = Crc16Utils.computeCrc(Crc16Utils.ANSI_CRC16_TABLE, 0, os.toByteArray());
        assertEquals(expected, crc);
    }

    public static Directory mkDir() {
        DirectoryHeader.DirectoryHeaderBuilder header = new DirectoryHeader.DirectoryHeaderBuilder()
                .setModifiedTimestamp(100)
                .setSystemTimestamp(200);

        FileEntry soleEntry = new FileEntry.FileEntryBuilder()
                .addAttributes(FileAttribute.READ)
                .setIndex(0)
                .setLength(8)
                .setId(1)
                .setTimeStamp(150)
                .setDataType(128) // fit file
                .create();
        Directory dir = new Directory.DirectoryBuilder()
                .setHeader(header)
                .addFile(soleEntry)
                .create();
        return dir;
    }
}
