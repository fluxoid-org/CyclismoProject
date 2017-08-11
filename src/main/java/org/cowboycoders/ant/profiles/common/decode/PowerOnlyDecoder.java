package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.utils.CounterBasedDecoder;
import org.cowboycoders.ant.profiles.common.decode.interfaces.PowerOnlyDecodable;
import org.cowboycoders.ant.profiles.common.events.AveragedPowerUpdate;
import org.cowboycoders.ant.profiles.common.events.InstantPowerUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;

/**
 * Gets Power data from an ant+ page that only contains power data. i.e not dervived from torque
 * Created by fluxoid on 04/01/17.
 */
public class PowerOnlyDecoder<T extends PowerOnlyDecodable> implements Decoder<T> {

    private final MyCounterBasedDecoder counterBasedDecoder;
    private long powerSum;

    public PowerOnlyDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
        counterBasedDecoder = new MyCounterBasedDecoder(updateHub);
        reset();
    }

    public void reset() {
        powerSum = 0;
    }

    public void update(T next) {
        counterBasedDecoder.update(next);
    }

    public void invalidate() {
        counterBasedDecoder.invalidate();
    }

    private class MyCounterBasedDecoder extends CounterBasedDecoder<PowerOnlyDecodable> {
        public MyCounterBasedDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
            super(updateHub);
        }


        @Override
        protected void onUpdate() {
            bus.send(new InstantPowerUpdate(getCurrentPage().getClass(),new BigDecimal(getCurrentPage().getInstantPower())));
        }

        @Override
        protected void onInitializeCounters() {
        }

        @Override
        protected void onValidDelta() {
            PowerOnlyDecodable next = getCurrentPage();
            PowerOnlyDecodable prev = getPreviousPage();
            powerSum += next.getSumPowerDelta(prev);
        }

        @Override
        protected void onNoCoast() {
            bus.send(new AveragedPowerUpdate(getCurrentPage().getClass() ,powerSum, getEvents()));
        }
    }
}
