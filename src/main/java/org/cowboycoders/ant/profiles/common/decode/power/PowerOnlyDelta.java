package org.cowboycoders.ant.profiles.common.decode.power;

/**
 * Created by fluxoid on 04/01/17.
 */
public class PowerOnlyDelta extends AbstractPowerData {
    private long eventDelta;
    private long powerDelta;

    public PowerOnlyDelta(long eventDelta, long powerDelta) {
        this.eventDelta = eventDelta;
        this.powerDelta = powerDelta;
    }

    public long getEventDelta() {
        return eventDelta;
    }

    public long getPowerDelta() {
        return powerDelta;
    }
}
