package org.cowboycoders.ant.profiles.common.decode.interfaces;

/**
 * Created by fluxoid on 05/01/17.
 */
public interface PowerOnlyDecodable extends AbstractPowerDecodable {


    /**
     * the delta between the last processed update of
     * running sum of instanteous power updated on each increment of event count
     */
    public long getSumPowerDelta(PowerOnlyDecodable old);


    /**
     * Accumulated power : running sum of instanteous power updated on each increment of event count
     * @return accumulated power
     */
    public int getSumPower();

    public int getInstantPower();
}
