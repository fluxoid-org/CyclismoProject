package org.cowboycoders.ant.profiles.simulators;

import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;

/**
 * Created by fluxoid on 01/02/17.
 */
public interface PageGen {
    AntPacketEncodable getPageEncoder();
}
