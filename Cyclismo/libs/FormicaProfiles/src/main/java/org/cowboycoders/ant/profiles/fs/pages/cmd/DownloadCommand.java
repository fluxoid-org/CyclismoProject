package org.cowboycoders.ant.profiles.fs.pages.cmd;

import org.cowboycoders.ant.profiles.fs.pages.CommandFactory;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

public class DownloadCommand implements AntPage {

    private final int index;
    private final int offset; // requested offset
    private final boolean firstRequest;

    public boolean isFirstRequest() {
        return firstRequest;
    }

    // TODO: at index 9 of the burst containing this message is a flag indicating whether or not this was the original
    // request

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
        offset = view.unsignedToInt(4,4);
        firstRequest = view.unsignedToInt(9,1) != 0;

    }

    public int getOffset() {
        return offset;
    }
}
