package org.cowboycoders.ant.profiles.fs.pages.cmd;

import org.cowboycoders.ant.profiles.fs.pages.CommandFactory;
import org.cowboycoders.ant.profiles.pages.AntPage;


/**
 * Send as acknowledged message and wait for ack
 */
public class DisconnectCommand implements AntPage {

    @Override
    public int getPageNumber() {
        return CommandFactory.PAGE_NUM;
    }

}
