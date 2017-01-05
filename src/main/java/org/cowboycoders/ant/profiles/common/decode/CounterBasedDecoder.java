package org.cowboycoders.ant.profiles.common.decode;

/**
 * Created by fluxoid on 05/01/17.
 */
public interface CounterBasedDecoder {
    /**
     * reset state in the case that the counters have been reset
     */
    void invalidate();
}
