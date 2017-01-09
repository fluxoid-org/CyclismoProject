package org.cowboycoders.ant.profiles.common.decode.power;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.CoastDetector;
import org.cowboycoders.ant.profiles.common.decode.CounterBasedPage;
import org.cowboycoders.ant.profiles.common.telemetry.AveragedPowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.telemetry.PowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Gets Power data from an ant+ page that only contains power data. i.e not dervived from torque
 * Created by fluxoid on 04/01/17.
 */
public class PowerOnlyDecoder extends org.cowboycoders.ant.profiles.common.decode.CounterBasedDecoder {

    private long powerSum;

    PowerOnlyDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        super(updateHub);
    }

    @Override
    protected void onInitializeCounters(CounterBasedPage newPage) {
        powerSum = ((PowerOnlyPage)newPage).getSumPower();
    }

    @Override
    protected void onValidDelta(CounterBasedPage newPage, CounterBasedPage oldPage) {
        PowerOnlyPage next = (PowerOnlyPage) newPage;
        PowerOnlyPage prev = (PowerOnlyPage) oldPage;
        powerSum += next.getSumPowerDelta(prev);
    }

    @Override
    protected void onUpdate() {
        bus.sendMessage(new AveragedPowerUpdate(powerSum, getEvents()));
    }

}
