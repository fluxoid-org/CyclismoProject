package org.cowboycoders.ant.profiles.common.decode.interfaces;


// May have to specialise at a later date if provides multiple sources of rotations

/**
 * Generic rotations
 * Created by fluxoid on 16/01/17.
 */
public interface RotationDecodable {
    /**
     * wheel rotation period summation
     * @return
     */
    int getRotationPeriod();

    /**
     * wheel rotation delta relative to old
     */
    long getRotationPeriodDelta(RotationDecodable old);
}
