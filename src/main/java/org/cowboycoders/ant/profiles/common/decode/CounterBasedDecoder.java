package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.power.PowerOnlyPage;
import org.cowboycoders.ant.profiles.common.telemetry.AveragedPowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.telemetry.PowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 05/01/17.
 */
public abstract class CounterBasedDecoder {
    protected final BroadcastMessenger<TelemetryEvent> bus;
    private CoastDetector coastDetector = new CoastDetector();
    private PowerOnlyPage prev;
    private long events;

    public CounterBasedDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        if (updateHub == null) {
            throw new IllegalArgumentException("this bus cannot be null");
        }
        this.bus = updateHub;
    }

    private void initializeCounters(CounterBasedPage next) {
        events = next.getEventCount();
        onInitializeCounters(next);
    }

    protected long getEvents() {
        return events;
    }

    protected abstract void onInitializeCounters(CounterBasedPage newPage);

    protected abstract void onValidDelta(CounterBasedPage newPage, CounterBasedPage oldPage);

    protected abstract void onUpdate();

    public void update(PowerOnlyPage next) {
        coastDetector.update(next);
        bus.sendMessage(new PowerUpdate(new BigDecimal(next.getInstantPower())));
        if (!next.isValidDelta(prev) || prev == null) {
            coastDetector.startCoast(next);
            prev = next;
            initializeCounters(next);
            return;
        }

        events += next.getEventCountDelta(prev);
        onValidDelta(next, prev);

        if (next.getEventCountDelta(prev) == 0) {
            coastDetector.startCoast(prev);
        } else {
            coastDetector.stopCoast();
        }
        if (coastDetector.isCoasting()) {
            bus.sendMessage(new CoastDetectedEvent());
        } else {
            onUpdate();
        }
        prev = next;
    }

    public void invalidate() {
        prev = null;
        coastDetector.stopCoast();
    }
}
