package org.cowboycoders.ant.profiles.pages;

import org.cowboycoders.ant.profiles.pages.AntPage;

/**
 * Created by fluxoid on 30/12/16.
 */
public interface AntPageFactory {
    public AntPage decode(byte[] data);
}
