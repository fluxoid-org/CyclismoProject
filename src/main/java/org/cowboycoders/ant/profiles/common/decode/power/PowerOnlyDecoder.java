package org.cowboycoders.ant.profiles.common.decode.power;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.CoastDetector;
import org.cowboycoders.ant.profiles.common.telemetry.AveragedPowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.telemetry.PowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Gets Power data from an ant+ page that only contains power data. i.e not dervived from torque
 * Created by fluxoid on 04/01/17.
 */
public class PowerOnlyDecoder implements org.cowboycoders.ant.profiles.common.decode.CounterBasedDecoder {

    private final BroadcastMessenger<TelemetryEvent> bus;
    private CoastDetector coastDetector = new CoastDetector();
    private PowerOnlyPage prev;
    private long events;
    private long powerSum;

    PowerOnlyDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        if (updateHub == null) {
            throw new IllegalArgumentException("this bus cannot be null");
        }
        this.bus = updateHub;
    }

    private void initializeCounters(PowerOnlyPage next) {
        events = next.getEventCount();
        powerSum = next.getSumPower();
    }

    public void update(PowerOnlyPage next) {
        coastDetector.update(next);
        bus.sendMessage(new PowerUpdate(new BigDecimal(next.getInstantPower())));
        if (!next.isValidDelta(prev) || prev == null) {
            prev = next;
            initializeCounters(next);
            return;
        }

        events += next.getEventCountDelta(prev);
        powerSum += next.getSumPowerDelta(prev);

        if (next.getEventCountDelta(prev) == 0) {
            coastDetector.startCoast(next);
            prev = next;
            return;
        }
        if (coastDetector.isCoasting()) {
            bus.sendMessage(new CoastDetectedEvent());
        } else {
            coastDetector.stopCoast();
            bus.sendMessage(new AveragedPowerUpdate(powerSum, events));
        }
        prev = next;
    }

    @Override
    public void invalidate() {
        prev = null;
        coastDetector.stopCoast();
    }
}
