package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.telemetry.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.telemetry.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 05/01/17.
 */
public abstract class CounterBasedDecoder<T extends CounterBasedPage> {
    protected final BroadcastMessenger<TelemetryEvent> bus;
    private CoastDetector coastDetector = new CoastDetector();
    private T prev;
    private T currentPage;
    private long events;

    public T getPreviousPage() {
        return prev;
    }

    public T getCurrentPage() {
        return currentPage;
    }

    public CounterBasedDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        if (updateHub == null) {
            throw new IllegalArgumentException("this bus cannot be null");
        }
        this.bus = updateHub;
    }

    private void initializeCounters(T next) {
        events = next.getEventCount();
        onInitializeCounters();
    }

    protected long getEvents() {
        return events;
    }

    protected abstract void onInitializeCounters();

    protected abstract void onValidDelta();

    protected abstract void onNoCoast();

    public void update(T next) {
        this.currentPage = next;
        onUpdate();
        coastDetector.update(next);
        if (!next.isValidDelta(prev) || prev == null) {
            coastDetector.startCoast(next);
            prev = next;
            initializeCounters(next);
            return;
        }

        events += next.getEventCountDelta(prev);
        onValidDelta();

        if (next.getEventCountDelta(prev) == 0) {
            coastDetector.startCoast(prev);
        } else {
            coastDetector.stopCoast();
        }
        if (coastDetector.isCoasting()) {
            bus.sendMessage(new CoastDetectedEvent());
        } else {
            onNoCoast();
        }
        prev = next;
    }

    protected abstract void onUpdate();

    public void invalidate() {
        prev = null;
        coastDetector.stopCoast();
    }
}
