package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

/**
 * When a lower priority event is encountered it assumed to be a duplicate of an already transmitted event and is
 * suppressed. If no other lower priority events are received by the time another matching event is fired, this new event
 * will be passed through.
 */
public class BufferedEventPrioritiser extends EventPrioritiser {

    /**
     * @param out        forwards unfiltered events here
     * @param priorities events to be prioritised. If an event not in this array is received, it will be passed through
     */
    public BufferedEventPrioritiser(FilteredBroadcastMessenger<TaggedTelemetryEvent> out, PrioritisedEvent[] priorities) {
        super(out, priorities);
    }


    @Override
    protected void onMatchingPriority(TaggedTelemetryEvent telemetryEvent, Class<? extends TaggedTelemetryEvent> clazz, long timeStamp) {
        // event locked in
        EventPrioritiser.TimeStampPair lastPair = getLastUpdates().get(clazz);
        accept(telemetryEvent);
        store(telemetryEvent, clazz, timeStamp);
    }

    @Override
    protected void onLowerPriority(TaggedTelemetryEvent telemetryEvent, Class<? extends TaggedTelemetryEvent> clazz, long timeStamp) {
        // we assume we have already received a value for this event and skip til next matching packet
        store(telemetryEvent, clazz, timeStamp);
    }

    @Override
    protected void onFirstUpdate(TaggedTelemetryEvent telemetryEvent, Class<? extends TaggedTelemetryEvent> clazz, long newStamp) {
        accept(telemetryEvent);
        store(telemetryEvent, clazz, getTimeStamp());
    }
}
