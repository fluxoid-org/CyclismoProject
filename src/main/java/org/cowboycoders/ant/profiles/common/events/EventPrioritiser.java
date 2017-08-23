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
        PrioritisedEvent pri = EventPrioritiser.this.priorities.get(clazz);
        TimeStampPair lastPair = pri.getLast(telemetryEvent);
        Hooks classHooks = getHooks();
        pri.handle(telemetryEvent, timeStamp, classHooks);
    }


    protected PrioritisedEvent getPrioritisedEvent(Class<? extends TaggedTelemetryEvent> clazz) {
        return priorities.get(clazz);
    }


    private void doAccept(TaggedTelemetryEvent telemetryEvent, long newStamp, PrioritisedEvent pri) {
        pri.setLast(new TimeStampPair(newStamp, telemetryEvent));
        accept(telemetryEvent);
    }


    protected void accept(TaggedTelemetryEvent telemetryEvent) {
        out.send(telemetryEvent);
    }

    public static class PrioritisedEvent {

        private final HashMap<Class<?>, Integer> tagPriorities = new HashMap<>();
        private final HashMap<Class<? extends TaggedTelemetryEvent>, Integer>  instancePriorities = new HashMap<>();
        private final Class<? extends TaggedTelemetryEvent> event;
        private final long timeout;
        private TimeStampPair last;

        public Class<? extends TaggedTelemetryEvent> getEvent() {
            return event;
        }

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

        public TimeStampPair getLast(TaggedTelemetryEvent newEvent) {
            return last;
        }

        public void setLast(TimeStampPair newValue) {
            this.last = newValue;
        }

        public void handle(TaggedTelemetryEvent telemetryEvent, long timeStamp,
                           Hooks hooks) {
            TimeStampPair lastPair = getLast(telemetryEvent);
            if (lastPair == null) {
                // no previous data
                hooks.onFirstUpdate(telemetryEvent, timeStamp, this);
                return;
            }

            TaggedTelemetryEvent last = lastPair.event;

            // handle timeout

            if (timeStamp - lastPair.timeStamp > timeout) {
                hooks.onLowerPriority(telemetryEvent, timeStamp, this);
                return;
            }

            Class<? extends TaggedTelemetryEvent> clazz = event;
            if (last.getClass().equals(telemetryEvent.getClass()) && last.getTag().getClass().equals(
                    telemetryEvent.getTag().getClass()
            )) {
                hooks.onAccepted(telemetryEvent, timeStamp, this);
            } else if (getInstancePriority(telemetryEvent) < getInstancePriority(last)) {
                hooks.onLowerPriority(telemetryEvent, timeStamp, this);
            } else if (getInstancePriority(telemetryEvent).equals(getInstancePriority(last))
                    && getTagPriority(telemetryEvent) < getTagPriority(last)) {
                hooks.onLowerPriority(telemetryEvent, timeStamp, this);
            }

            // filter
        }

        protected Integer getTagPriority(TaggedTelemetryEvent telemetryEvent) {
            Integer result =  tagPriorities.get(telemetryEvent.getTag().getClass());
            if (result == null) return Integer.MAX_VALUE;
            return result;
        }

        protected Integer getInstancePriority(TaggedTelemetryEvent telemetryEvent) {
            Integer result = instancePriorities.get(telemetryEvent.getClass());
            if (result == null) return Integer.MAX_VALUE;
            return result;
        }
    }

    protected interface Hooks {
        void onAccepted(TaggedTelemetryEvent event, long timeStamp, PrioritisedEvent prioritisedEvent);
        void onFirstUpdate(TaggedTelemetryEvent event, long timeStamp, PrioritisedEvent prioritisedEvent);
        void onLowerPriority(TaggedTelemetryEvent event, long timeStamp, PrioritisedEvent prioritisedEvent);
    }

    private Hooks hooks = new Hooks() {

        @Override
        public void onAccepted(TaggedTelemetryEvent event, long timeStamp, PrioritisedEvent prioritisedEvent) {
            doAccept(event, timeStamp, prioritisedEvent);
        }

        @Override
        public void onFirstUpdate(TaggedTelemetryEvent event, long timeStamp, PrioritisedEvent prioritisedEvent) {
            doAccept(event, timeStamp, prioritisedEvent);
        }

        @Override
        public void onLowerPriority(TaggedTelemetryEvent event, long timeStamp, PrioritisedEvent prioritisedEvent) {
            doAccept(event, timeStamp, prioritisedEvent);
        }
    };

    protected Hooks getHooks() {
        return this.hooks;
    }

    // all sub classes inherit tag priorities
    public static class InheritedPrioritisedEvent extends PrioritisedEvent {

        private HashMap<Class<? extends TaggedTelemetryEvent>, TimeStampPair> lastUpdates = new HashMap<>();

        @SuppressWarnings("unchecked")
        public InheritedPrioritisedEvent(Class<? extends TaggedTelemetryEvent> event,
                                         long timeoutNanos,
                                         Class<?> [] tagPriorities) {
            super(event, timeoutNanos, tagPriorities, new Class[0]);
        }

        @Override
        public TimeStampPair getLast(TaggedTelemetryEvent event) {
            return lastUpdates.get(event.getClass());
        }

        @Override
        public void setLast(TimeStampPair newValue) {
            // different classes are distinct events in this subclass
            lastUpdates.put(newValue.event.getClass(), newValue);
        }

    }

    // prioritised event to priorities
    private HashMap<Class<? extends TaggedTelemetryEvent>, PrioritisedEvent> priorities = new HashMap<>();
    // telemetry event to base class in priorities list
    private HashMap<Class<? extends TaggedTelemetryEvent>, Class<? extends TaggedTelemetryEvent>> mappings = new HashMap<>();
    // all events that we filter
    private ArrayList<Class<? extends TaggedTelemetryEvent>> allPrioritised = new ArrayList<>();

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

}
