package org.cowboycoders.ant.profiles.common.decode.power;

/**
 * Created by fluxoid on 05/01/17.
 */
public interface PowerOnlyPage extends AbstractPowerPage {


    /**
     * the delta between the last processed update of
     * running sum of instanteous power updated on each increment of event count
     */
    public long getSumPowerDelta(PowerOnlyPage old);


    /**
     *  Number of event 'ticks' since last update
     */
    public long getEventCountDelta(PowerOnlyPage old);


    /**
     * Accumulated power : running sum of instanteous power updated on each increment of event count
     * @return accumulated power
     */
    public int getSumPower();

    /**
     * Number of updates of accumulated power such that {@code getSumPower / getEventCount} is
     * the average power of that interval
     */
    public int getEventCount();

    public boolean isValidDelta(PowerOnlyPage old);
}
