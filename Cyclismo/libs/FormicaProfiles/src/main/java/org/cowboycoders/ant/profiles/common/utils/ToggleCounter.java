package org.cowboycoders.ant.profiles.common.utils;

/**
 * Created by fluxoid on 29/12/16.
 */
public class ToggleCounter {

    private boolean isInitialized;
    private boolean state;
    private long count = 0;

    public ToggleCounter() {
        this.isInitialized = false;
    }

    /**
     * Counts toggle iff state has changed.
     * @param state of switch
     * @return true if state has been updated, false otherwise. Will always return false on first call.
     */
    public boolean toggle(boolean state) {
        if (!isInitialized) {
            this.state = state;
            isInitialized = true;
            return false;
        }
        if (state != this.state) {
            this.state = state;
            count ++;
            return true;
        }
        return true;
    }

    public long getCount() {
        return count;
    }
}
