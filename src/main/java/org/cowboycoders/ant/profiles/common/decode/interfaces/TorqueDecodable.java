package org.cowboycoders.ant.profiles.common.decode.interfaces;

/**
 * Created by fluxoid on 09/01/17.
 */
public interface TorqueDecodable extends AbstractPowerDecodable, RotationDecodable {

    long getTorqueDelta(TorqueDecodable old);
    int getTorque();


}
