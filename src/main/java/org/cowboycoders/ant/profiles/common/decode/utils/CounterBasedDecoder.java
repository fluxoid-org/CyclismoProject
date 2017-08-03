package org.cowboycoders.ant.profiles.common.decode.utils;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CounterBasedDecodable;
import org.cowboycoders.ant.profiles.common.events.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

/**
 * Created by fluxoid on 05/01/17.
 */
public abstract class CounterBasedDecoder<T extends CounterBasedDecodable> {
    protected final FilteredBroadcastMessenger<TelemetryEvent> bus;
    private CoastDetector coastDetector = new CoastDetector();
    private T prev;
    private T currentPage;
    private long events;
    private long eventDelta;

    public T getPreviousPage() {
        return prev;
    }

    public T getCurrentPage() {
        return currentPage;
    }

    public CounterBasedDecoder(FilteredBroadcastMessenger<TelemetryEvent> updateHub) {
        if (updateHub == null) {
            throw new IllegalArgumentException("this bus cannot be null");
        }
        this.bus = updateHub;
        reset();
    }

    private void reset() {
        events = 0;
    }

    private void initializeCounters(T next) {
        onInitializeCounters();
    }

    public long getEventDelta() {
        return eventDelta;
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
        if (prev == null || !next.isValidDelta(prev)) {
            coastDetector.startCoast(next);
            prev = next;
            initializeCounters(next);
            return;
        }

        events += next.getEventCountDelta(prev);
        eventDelta = next.getEventCountDelta(prev);
        onValidDelta();

        if (next.getEventCountDelta(prev) == 0) {
            coastDetector.startCoast(prev);
        } else {
            coastDetector.stopCoast();
        }
        if (coastDetector.isCoasting()) {
            bus.send(new CoastDetectedEvent());
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
