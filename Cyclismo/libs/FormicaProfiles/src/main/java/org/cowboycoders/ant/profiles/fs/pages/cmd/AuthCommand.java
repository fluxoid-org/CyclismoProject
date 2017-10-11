package org.cowboycoders.ant.profiles.fs.pages.cmd;

import org.cowboycoders.ant.profiles.fs.pages.AuthMode;
import org.cowboycoders.ant.profiles.fs.pages.CommandFactory;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

public class AuthCommand implements AntPage {

    private final AuthMode mode;

    public AuthMode getMode() {
        return mode;
    }


    public AuthCommand(byte[] data) {
        LittleEndianArray view = new LittleEndianArray(data);
        this.mode = AuthMode.decode(data[2]);

    }

    @Override
    public int getPageNumber() {
        return CommandFactory.PAGE_NUM;
    }
}
