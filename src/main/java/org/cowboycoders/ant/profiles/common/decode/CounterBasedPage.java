package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.TelemetryPage;

/**
 * Created by fluxoid on 09/01/17.
 */
public interface CounterBasedPage extends TelemetryPage {
        /**
         *  Number of event 'ticks' since last update
         */
        long getEventCountDelta(CounterBasedPage old);

        /**
         * Number of updates of accumulated power such that {@code getSumPower / getEventCount} is
         * the average power of that interval
         */
        int getEventCount();

        boolean isValidDelta(CounterBasedPage old);
}
