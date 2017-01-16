package org.cowboycoders.ant.profiles.common.decode.interfaces;

/**
 * Created by fluxoid on 09/01/17.
 */
public interface TorqueDecodable extends AbstractPowerDecodable {

    long getTorqueDelta(TorqueDecodable old);
    int getTorque();

    /**
     * wheel rotation period summation
     * @return
     */
    int getPeriod();


    /**
     * wheel rotation delta relative to old
     */
    long getPeriodDelta(TorqueDecodable old);

}
