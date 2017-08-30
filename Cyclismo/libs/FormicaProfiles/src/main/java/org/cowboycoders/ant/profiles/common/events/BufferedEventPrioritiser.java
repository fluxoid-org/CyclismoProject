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

    private Hooks hooks = new Hooks() {
        @Override
        public void onAccepted(TaggedTelemetryEvent event, long timeStamp, PrioritisedEvent prioritisedEvent) {
            accept(event);
            prioritisedEvent.setLast(new TimeStampPair(timeStamp, event));
        }

        @Override
        public void onFirstUpdate(TaggedTelemetryEvent event, long timeStamp, PrioritisedEvent prioritisedEvent) {
            accept(event);
            prioritisedEvent.setLast(new TimeStampPair(timeStamp, event));
        }

        @Override
        public void onLowerPriority(TaggedTelemetryEvent event, long timeStamp, PrioritisedEvent prioritisedEvent) {
            prioritisedEvent.setLast(new TimeStampPair(timeStamp, event));
        }
    };

    @Override
    protected Hooks getHooks() {
        return hooks;
    }
}
