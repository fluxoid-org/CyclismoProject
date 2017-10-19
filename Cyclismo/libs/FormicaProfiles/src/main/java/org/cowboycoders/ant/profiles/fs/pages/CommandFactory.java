package org.cowboycoders.ant.profiles.fs.pages;

import org.cowboycoders.ant.profiles.fs.defines.Command;
import org.cowboycoders.ant.profiles.fs.pages.cmd.DisconnectCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.DownloadCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.LinkCommand;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.util.logging.Logger;

public class CommandFactory {
    public static final  int PAGE_NUM = 68;
    private static Logger LOGGER = Logger.getLogger(CommandFactory.class.getName());

    public static AntPage decode(byte [] data) {
        Command cmd =  Command.decode(data,1);
        try {
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
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            LOGGER.severe("incomplete packet");
        }
        return null;
    }
}
