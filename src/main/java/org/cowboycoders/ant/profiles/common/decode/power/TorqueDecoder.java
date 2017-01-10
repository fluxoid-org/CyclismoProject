package org.cowboycoders.ant.profiles.common.decode.power;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.CounterBasedDecoder;
import org.cowboycoders.ant.profiles.common.decode.Decoder;
import org.cowboycoders.ant.profiles.common.events.TelemetryEvent;
import org.cowboycoders.ant.profiles.common.events.TorquePowerUpdate;
import org.cowboycoders.ant.profiles.common.events.TorqueUpdate;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Gets Power data from an ant+ page that contains power data dervived from torque
 * Created by fluxoid on 04/01/17.
 */
public class TorqueDecoder implements Decoder<TorquePage> {

    private final MyCounterBasedDecoder counterBasedDecoder;
    private long torqueSum;
    private long periodSum;

    TorqueDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        counterBasedDecoder = new MyCounterBasedDecoder(updateHub);
    }

    public void update(TorquePage next) {
        counterBasedDecoder.update(next);
    }

    public void invalidate() {
        counterBasedDecoder.invalidate();
    }

    private class MyCounterBasedDecoder extends CounterBasedDecoder<TorquePage> {
        public MyCounterBasedDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
            super(updateHub);
        }


        @Override
        protected void onUpdate() {
            //
        }

        @Override
        protected void onInitializeCounters() {
            torqueSum = getCurrentPage().getTorque();
            periodSum = getCurrentPage().getPeriod();
        }

        @Override
        protected void onValidDelta() {
            TorquePage next = getCurrentPage();
            TorquePage prev = getPreviousPage();
            torqueSum = next.getTorqueDelta(prev);
            periodSum = next.getTorqueDelta(prev);
        }

        @Override
        protected void onNoCoast() {
            bus.sendMessage(new TorquePowerUpdate(torqueSum, periodSum));
            BigDecimal torque = new BigDecimal(torqueSum).divide(new BigDecimal(32), 15, RoundingMode.HALF_UP).divide(new BigDecimal(getEventDelta()), 13, RoundingMode.HALF_UP);
            bus.sendMessage(new TorqueUpdate(torque));
        }

    }
}
