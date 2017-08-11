package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.utils.CounterBasedDecoder;
import org.cowboycoders.ant.profiles.common.decode.interfaces.TorqueDecodable;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.cowboycoders.ant.profiles.common.events.TorquePowerUpdate;
import org.cowboycoders.ant.profiles.common.events.TorqueUpdate;
import org.cowboycoders.ant.profiles.common.events.AverageTorqueUpdate;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Gets Power data from an ant+ page that contains power data dervived from torque
 * Created by fluxoid on 04/01/17.
 */
public class TorqueDecoder<T extends TorqueDecodable> implements Decoder<T> {

    private final MyCounterBasedDecoder counterBasedDecoder;
    private long torqueDelta;
    private long periodDelta;
    private long periodSum;
    private long torqueSum;

    public void reset() {
        torqueDelta = 0;
        periodDelta = 0;
        torqueSum = 0;
        periodSum = 0;
    }

    TorqueDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
        counterBasedDecoder = new MyCounterBasedDecoder(updateHub);
        reset();
    }

    public void update(T next) {
        counterBasedDecoder.update(next);
    }

    public void invalidate() {
        counterBasedDecoder.invalidate();
    }

    private class MyCounterBasedDecoder extends CounterBasedDecoder<TorqueDecodable> {
        public MyCounterBasedDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
            super(updateHub);
        }


        @Override
        protected void onUpdate() {
            //
        }

        @Override
        protected void onInitializeCounters() {
            //
        }

        @Override
        protected void onValidDelta() {
            TorqueDecodable next = getCurrentPage();
            TorqueDecodable prev = getPreviousPage();
            torqueDelta = next.getRawTorqueDelta(prev);
            periodDelta = next.getRotationPeriodDelta(prev);
            torqueSum += torqueDelta;
            periodSum += periodDelta;
        }

        @Override
        protected void onNoCoast() {
            bus.send(new TorquePowerUpdate(getCurrentPage().getClass() ,torqueDelta, periodDelta));
            BigDecimal torque = new BigDecimal(torqueDelta).divide(new BigDecimal(32), 15, RoundingMode.HALF_UP).divide(new BigDecimal(getEventDelta()), 13, RoundingMode.HALF_UP);
            bus.send(new TorqueUpdate(getCurrentPage().getClass() ,torque));
            bus.send(new AverageTorqueUpdate(getCurrentPage().getClass(),periodSum, torqueSum, getEvents()));
        }

    }
}
