package org.cowboycoders.ant.profiles.fs.pages.cmd;

import org.cowboycoders.ant.profiles.fs.pages.CommandFactory;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

public class DownloadCommand implements AntPage {

    private final int index;

    /**
     * File index 0 is directory listing
     * @return file index
     */
    public int getIndex() {
        return index;
    }

    @Override
    public int getPageNumber() {
        return CommandFactory.PAGE_NUM;
    }

    public DownloadCommand(byte [] data) {
        LittleEndianArray view  = new LittleEndianArray(data);
        index = view.unsignedToInt(2,2);
    }
}
