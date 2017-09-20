package org.cowboycoders.ant.profiles.fs.pages.cmd;

import org.cowboycoders.ant.profiles.fs.defines.Command;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

public class LinkCommand implements AntPage {

    private final byte requestedFrequency;
    private final int serialNumber;
    private static byte [] freqTable  = new byte[] { 3, 7, 15, 20, 25, 29, 34, 40, 45, 49, 54, 60, 65, 70, 75, 80 };

    public static final int PAGE_NUM = 68;


    @Override
    public int getPageNumber() {
        return PAGE_NUM;
    }

    public LinkCommand(byte [] data) {
        LittleEndianArray view = new LittleEndianArray(data);
        this.requestedFrequency = data[2];
        this.serialNumber = view.unsignedToInt(4,4);
        // byte 3 is always 4?
    }

    public int getRequestedFrequency() {
        // for easy consumption by Channel which takes an int
        return 0xff & requestedFrequency;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

}
