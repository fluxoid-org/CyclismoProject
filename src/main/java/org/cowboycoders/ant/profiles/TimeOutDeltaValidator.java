package org.cowboycoders.ant.profiles;

import org.cowboycoders.ant.profiles.common.decode.CounterBasedPage;

public class TimeOutDeltaValidator {
    private final long delta;

    public TimeOutDeltaValidator(long delta) {
        this.delta = delta;
    }

    public boolean isValidDelta(CounterBasedPage old, CounterBasedPage latest) {
        if (latest.getTimestamp() - old.getTimestamp() >= delta) {
            return false;
        }
        return true;
    }
}