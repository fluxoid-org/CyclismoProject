package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Filters events by priority. If a message is not received within the timeout window a lower priority message
 * will be accepted.
 */
public class EventPrioritiser implements BroadcastListener<TaggedTelemetryEvent> {

    private final FilteredBroadcastMessenger<TaggedTelemetryEvent> out;

    protected class TimeStampPair {
        private TaggedTelemetryEvent event;
        private long timeStamp;

        public TaggedTelemetryEvent getEvent() {
            return event;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public TimeStampPair(long newStamp, TaggedTelemetryEvent telemetryEvent) {
            this.timeStamp = newStamp;
            this.event = telemetryEvent;
        }
    }

    /**
     * Override for testing purposes. Should only be called one per received message.
     * @return nano second precision timestamp
     */
    public long getTimeStamp() {
        return System.nanoTime();
    }

    @Override
    public void receiveMessage(TaggedTelemetryEvent telemetryEvent) {
        final long timeStamp = getTimeStamp();

        if (!mappings.containsKey(telemetryEvent.getClass())) {
            OUTER :{
                for (Class<? extends TaggedTelemetryEvent> clazz : allPrioritised) {
                    if (clazz.isInstance(telemetryEvent)) {
                        mappings.put(telemetryEvent.getClass(), clazz);
                        break OUTER;
                    }
                }
                // not one of the prioritised messages
                mappings.put(telemetryEvent.getClass(), null);
            }

        }

        Class<? extends TaggedTelemetryEvent> clazz = mappings.get(telemetryEvent.getClass());
        if (clazz == null) { // no mapping
            out.send(telemetryEvent);
            return;
        }
        TimeStampPair lastPair = lastUpdates.get(clazz);
        if (lastPair == null) {
            // no previous data
            onFirstUpdate(telemetryEvent, clazz, timeStamp);
            return;
        }

        TaggedTelemetryEvent last = lastPair.event;

        // handle timeout
        PrioritisedEvent pri = EventPrioritiser.this.priorities.get(clazz);

        if (timeStamp - lastPair.timeStamp > pri.timeout) {
            onLowerPriority(telemetryEvent, clazz, timeStamp);
            return;
        }
        if (last.getClass().equals(telemetryEvent.getClass()) && last.getTag().getClass().equals(
                telemetryEvent.getTag().getClass()
        )) {
            onMatchingPriority(telemetryEvent, clazz, timeStamp);
        } else if (getInstancePriority(telemetryEvent, pri) < getInstancePriority(last, pri)) {
            onLowerPriority(telemetryEvent, clazz, timeStamp);
        } else if (getInstancePriority(telemetryEvent, pri).equals(getInstancePriority(last,pri))
                && getTagPriority(telemetryEvent,pri) < getTagPriority(last, pri)) {
            onLowerPriority(telemetryEvent, clazz, timeStamp);
        }

        // filter
    }

    protected void onMatchingPriority(TaggedTelemetryEvent telemetryEvent, Class<? extends TaggedTelemetryEvent> clazz, long timeStamp) {
        doAccept(telemetryEvent, clazz, timeStamp);
    }

    protected void onLowerPriority(TaggedTelemetryEvent telemetryEvent, Class<? extends TaggedTelemetryEvent> clazz, long timeStamp) {
        doAccept(telemetryEvent, clazz, timeStamp);
    }

    protected PrioritisedEvent getPrioritisedEvent(Class<? extends TaggedTelemetryEvent> clazz) {
        return priorities.get(clazz);
    }

    protected void onFirstUpdate(TaggedTelemetryEvent telemetryEvent, Class<? extends TaggedTelemetryEvent> clazz, long newStamp) {
        doAccept(telemetryEvent, clazz, newStamp);
    }

    private void doAccept(TaggedTelemetryEvent telemetryEvent, Class<? extends TaggedTelemetryEvent> clazz, long newStamp) {
        store(telemetryEvent, clazz, newStamp);
        accept(telemetryEvent);
    }

    protected void store(TaggedTelemetryEvent telemetryEvent, Class<? extends TaggedTelemetryEvent> clazz, long newStamp) {
        lastUpdates.put(clazz, new TimeStampPair(newStamp, telemetryEvent));
    }

    protected void accept(TaggedTelemetryEvent telemetryEvent) {
        out.send(telemetryEvent);
    }

    public static class PrioritisedEvent {

        private final HashMap<Class<?>, Integer> tagPriorities = new HashMap<>();
        private final HashMap<Class<? extends TaggedTelemetryEvent>, Integer>  instancePriorities = new HashMap<>();
        private final Class<? extends TaggedTelemetryEvent> event;
        private final long timeout;

        /**
         *
         * @param event generic base
         * @param timeoutNanos before a previous update is considered stale
         * @param instancePriorities ordered list of priorities, more specific first
         *                           (first matching class is used for filtering)
         * @param tagPriorities higher priority first
         */
        public PrioritisedEvent(Class<? extends TaggedTelemetryEvent> event,
                                long timeoutNanos,
                                Class<?> [] tagPriorities,
                                Class<? extends TaggedTelemetryEvent> [] instancePriorities) {
            this.event = event;
            for (int i = 0; i < instancePriorities.length; i++) {
                this.instancePriorities.put(instancePriorities[i], i);
            }
            for (int i = 0; i < tagPriorities.length; i++) {
                this.tagPriorities.put(tagPriorities[i], i);
            }
            this.timeout = timeoutNanos;
        }
    }

    // prioritised event to last received example
    private HashMap<Class<? extends TaggedTelemetryEvent>, TimeStampPair> lastUpdates = new HashMap<>();
    // prioritised event to priorities
    private HashMap<Class<? extends TaggedTelemetryEvent>, PrioritisedEvent> priorities = new HashMap<>();
    // telemetry event to base class in priorities list
    private HashMap<Class<? extends TaggedTelemetryEvent>, Class<? extends TaggedTelemetryEvent>> mappings = new HashMap<>();
    // all events that we filter
    private ArrayList<Class<? extends TaggedTelemetryEvent>> allPrioritised = new ArrayList<>();

    protected HashMap<Class<? extends TaggedTelemetryEvent>, TimeStampPair> getLastUpdates() {
        return lastUpdates;
    }

    /**
     *
     * @param out forwards unfiltered events here
     * @param priorities events to be prioritised. If an event not in this array is received, it will be passed through
     */
    public EventPrioritiser(final FilteredBroadcastMessenger<TaggedTelemetryEvent> out,
                            final PrioritisedEvent[] priorities) {
        for (PrioritisedEvent p : priorities) {
            allPrioritised.add(p.event);
            this.priorities.put(p.event, p);
        }
        this.out = out;

    }

    protected Integer getTagPriority(TaggedTelemetryEvent telemetryEvent, PrioritisedEvent pri) {
        Integer result =  pri.tagPriorities.get(telemetryEvent.getTag().getClass());
        if (result == null) return Integer.MAX_VALUE;
        return result;
    }

    protected Integer getInstancePriority(TaggedTelemetryEvent telemetryEvent, PrioritisedEvent pri) {
        Integer result = pri.instancePriorities.get(telemetryEvent.getClass());
        if (result == null) return Integer.MAX_VALUE;
        return result;
    }
}
