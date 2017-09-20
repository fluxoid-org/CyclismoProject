package org.cowboycoders.ant.profiles.fs.pages.cmd;

import org.cowboycoders.ant.profiles.fs.pages.CommandFactory;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

public class AuthCommand implements AntPage {

    private final AuthMode mode;

    public AuthMode getMode() {
        return mode;
    }

    public enum AuthMode {
        PASSTHROUGH,
        SERIAL,
        PAIRING,
        PASSKEY,
        UNKNOWN;

        public static AuthMode[] vals = AuthMode.values();

        public static AuthMode decode(byte [] data) {
            int i = 0xff & data[2];
            if (i >= vals.length) {
                return UNKNOWN;
            }
            return vals[i];
        }

    }


    public AuthCommand(byte[] data) {
        LittleEndianArray view = new LittleEndianArray(data);
        this.mode = AuthMode.decode(data);

    }

    @Override
    public int getPageNumber() {
        return CommandFactory.PAGE_NUM;
    }
}
