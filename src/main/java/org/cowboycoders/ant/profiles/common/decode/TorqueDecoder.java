package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.utils.CounterBasedDecoder;
import org.cowboycoders.ant.profiles.common.decode.interfaces.TorqueDecodable;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;
import org.cowboycoders.ant.profiles.common.events.TorquePowerUpdate;
import org.cowboycoders.ant.profiles.common.events.TorqueUpdate;
import org.cowboycoders.ant.profiles.common.events.AverageTorqueUpdate;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Gets Power data from an ant+ page that contains power data dervived from torque
 * Created by fluxoid on 04/01/17.
 */
public class TorqueDecoder implements Decoder<TorqueDecodable> {

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

    TorqueDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        counterBasedDecoder = new MyCounterBasedDecoder(updateHub);
        reset();
    }

    public void update(TorqueDecodable next) {
        counterBasedDecoder.update(next);
    }

    public void invalidate() {
        counterBasedDecoder.invalidate();
    }

    private class MyCounterBasedDecoder extends CounterBasedDecoder<TorqueDecodable> {
        public MyCounterBasedDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
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
            periodDelta = next.getRawTorqueDelta(prev);
            torqueSum += torqueDelta;
            periodSum += periodDelta;
        }

        @Override
        protected void onNoCoast() {
            bus.sendMessage(new TorquePowerUpdate(torqueDelta, periodDelta));
            BigDecimal torque = new BigDecimal(torqueDelta).divide(new BigDecimal(32), 15, RoundingMode.HALF_UP).divide(new BigDecimal(getEventDelta()), 13, RoundingMode.HALF_UP);
            bus.sendMessage(new TorqueUpdate(torque));
            bus.sendMessage(new AverageTorqueUpdate(periodSum, torqueSum, getEvents()));
        }

    }
}
