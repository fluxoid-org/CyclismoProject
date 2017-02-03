package org.fluxoid.utils;

/**
 * Created by fluxoid on 02/02/17.
 */
public class RollOverVal {

    // could use big int if we are worried about overflows
    private final long rollover;
    private long currentVal;


    public RollOverVal(long maxVal) {
        this.rollover = maxVal + 1;
    }

    public void add(long n) {
        if (Long.MAX_VALUE - currentVal < n) {
            throw new IllegalStateException("internal variable would overflow");
        }
        currentVal += n;
    }

    /**
     * @param currentVal the unrolled over value
     */
    public void setValue(long currentVal) {
        assert currentVal >= 0;
        this.currentVal = currentVal;
    }

    /**
     * The unrolled-over value
     */
    public long getValue() {
        return currentVal;
    }

    public long get() {
        return currentVal % rollover;
    }

}
