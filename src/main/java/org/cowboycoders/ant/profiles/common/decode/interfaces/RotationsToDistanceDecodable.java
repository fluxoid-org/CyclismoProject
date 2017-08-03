package org.cowboycoders.ant.profiles.common.decode.interfaces;

/**
 * Provides number of rotations of bike wheel
 * Created by fluxoid on 10/01/17.
 */
public interface RotationsToDistanceDecodable extends CounterBasedDecodable {

    int getWheelRotations();
    long getWheelRotationsDelta(RotationsToDistanceDecodable old);

}
