package org.cowboycoders.ant.profiles.common;

/**
 * Created by fluxoid on 04/01/17.
 */
public abstract class AbstractTelemetryData {

    private final long timestamp;

    public AbstractTelemetryData() {
        timestamp = System.nanoTime();
    }

    /**
     *
     * @return as returned by @see System.nanoTime at construction
     */
    public long getTimestamp() {
        return timestamp;
    }
}
