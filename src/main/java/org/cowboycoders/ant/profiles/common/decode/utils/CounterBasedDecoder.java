package org.cowboycoders.ant.profiles.common.decode.utils;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CounterBasedDecodable;
import org.cowboycoders.ant.profiles.common.events.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.events.CoastEndEvent;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

/**
 * Created by fluxoid on 05/01/17.
 */
public abstract class CounterBasedDecoder<T extends CounterBasedDecodable> {
    protected final FilteredBroadcastMessenger<TaggedTelemetryEvent> bus;
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

    public CounterBasedDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
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

    private boolean sentCoast = false;

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
            doStopCoast();
        }
        if (coastDetector.isCoasting()) {
            sendCoastDetected();

        } else {
            onNoCoast();
        }
        prev = next;
    }

    // coasting hooks
    protected void onCoastStart() {}
    protected void onCoastStop()  {}

    private void sendCoastDetected() {
        if (!sentCoast) {
            onCoastStart();
            sentCoast = true;
        }
    }

    private void doStopCoast() {
        if (coastDetector.isCoasting())  {
            onCoastStart();
        }
        coastDetector.stopCoast();
        sentCoast = false;
    }

    protected abstract void onUpdate();

    public void invalidate() {
        prev = null;
        coastDetector.stopCoast();
    }
}
