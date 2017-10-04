package org.cowboycoders.ant.profiles.fs.pages;

import org.cowboycoders.ant.profiles.fs.defines.Command;
import org.cowboycoders.ant.profiles.fs.pages.cmd.DisconnectCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.DownloadCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.LinkCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.pages.AntPage;

public class CommandFactory {
    public static final  int PAGE_NUM = 68;

    public static AntPage decode(byte [] data) {
        Command cmd =  Command.decode(data,1);
        switch (cmd) {
            case CHANGE_FREQUENCY:
                return new LinkCommand(data);
            case REQUEST_AUTH:
                return new AuthCommand(data);
            case REQUEST_DOWNLOAD:
                return new DownloadCommand(data);
            case DISCONNECT:
                return new DisconnectCommand();
        }
        return null;
    }
}
