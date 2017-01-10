package org.cowboycoders.ant.profiles.common.decode.power;

/**
 * Created by fluxoid on 09/01/17.
 */
public interface TorquePage extends AbstractPowerPage {

    long getTorqueDelta(TorquePage old);
    int getTorque();

    /**
     * wheel rotation period summation
     * @return
     */
    int getPeriod();


    /**
     * wheel rotation delta relative to old
     */
    long getPeriodDelta(TorquePage old);

}
