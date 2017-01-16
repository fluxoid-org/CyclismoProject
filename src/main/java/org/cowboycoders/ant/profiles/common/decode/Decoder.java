package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.TelemetryPage;

/**
 * Created by fluxoid on 10/01/17.
 */
public interface Decoder<T> {
    void update(T newPage);

    /**
     * Invalidate the previous update so that the next update forms a reference for the next delta calculation
     */
    void invalidate();

    /**
     * Resets internal counters so that averages are recalculated
     */
    void reset();

}
