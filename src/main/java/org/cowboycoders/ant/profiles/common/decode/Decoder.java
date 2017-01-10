package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.TelemetryPage;

/**
 * Created by fluxoid on 10/01/17.
 */
public interface Decoder<T> {
    void update(T newPage);
    void invalidate();
}
