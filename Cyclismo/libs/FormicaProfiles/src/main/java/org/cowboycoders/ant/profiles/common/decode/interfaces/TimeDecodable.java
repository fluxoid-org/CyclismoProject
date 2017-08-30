package org.cowboycoders.ant.profiles.common.decode.interfaces;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 27/01/17.
 */
public interface TimeDecodable {

    /**
     * Seconds * 4
     * @return
     */
    public int getTicks();
    public long getTicksDelta(TimeDecodable old);
    public BigDecimal ticksToSeconds(long delta);
}
