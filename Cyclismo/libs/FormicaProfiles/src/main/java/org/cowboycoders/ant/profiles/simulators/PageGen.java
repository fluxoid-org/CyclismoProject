package org.cowboycoders.ant.profiles.simulators;

import org.cowboycoders.ant.profiles.pages.SinglePacketEncodable;

/**
 * Created by fluxoid on 01/02/17.
 */
public interface PageGen {
    SinglePacketEncodable getPageEncoder();
}
