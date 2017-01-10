package org.cowboycoders.ant.profiles.common.decode.power;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.CounterBasedDecoder;
import org.cowboycoders.ant.profiles.common.decode.Decoder;
import org.cowboycoders.ant.profiles.common.telemetry.AveragedPowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.InstantPowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Gets Power data from an ant+ page that only contains power data. i.e not dervived from torque
 * Created by fluxoid on 04/01/17.
 */
public class PowerOnlyDecoder implements Decoder<PowerOnlyPage> {

    private final MyCounterBasedDecoder counterBasedDecoder;
    private long powerSum;

    PowerOnlyDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        counterBasedDecoder = new MyCounterBasedDecoder(updateHub);
    }

    public void update(PowerOnlyPage next) {
        counterBasedDecoder.update(next);
    }

    public void invalidate() {
        counterBasedDecoder.invalidate();
    }

    private class MyCounterBasedDecoder extends CounterBasedDecoder<PowerOnlyPage> {
        public MyCounterBasedDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
            super(updateHub);
        }


        @Override
        protected void onUpdate() {
            bus.sendMessage(new InstantPowerUpdate(new BigDecimal(getCurrentPage().getInstantPower())));
        }

        @Override
        protected void onInitializeCounters() {
            powerSum = getCurrentPage().getSumPower();
        }

        @Override
        protected void onValidDelta() {
            PowerOnlyPage next = getCurrentPage();
            PowerOnlyPage prev = getPreviousPage();
            powerSum += next.getSumPowerDelta(prev);
        }

        @Override
        protected void onNoCoast() {
            bus.sendMessage(new AveragedPowerUpdate(powerSum, getEvents()));
        }
    }
}
