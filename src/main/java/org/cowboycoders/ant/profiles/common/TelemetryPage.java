package org.cowboycoders.ant.profiles.common;

/**
 * Created by fluxoid on 04/01/17.
 */
public interface TelemetryPage {

    /**
     * @return System.nanoTime() upon reception
     *
     */
    public long getTimestamp();
}
