package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Filters events by priority. If a message is not received within the timeout window a lower priority message
 * will be accepted.
 */
public class EventPrioritiser implements BroadcastListener<TaggedTelemetryEvent> {

    private final FilteredBroadcastMessenger<TaggedTelemetryEvent> out;

    @Override
    public void receiveMessage(TaggedTelemetryEvent telemetryEvent) {
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
        TaggedTelemetryEvent last = lastUpdates.get(clazz);
        if (last == null) {
            // no previous data
            accept(telemetryEvent, clazz);
            return;
        }

        // handle timeout

        if (last.getClass().equals(telemetryEvent.getClass())) {
            // last update was same type
            accept(telemetryEvent, clazz);
            return;
        }

        Priorities pri = EventPrioritiser.this.priorities.get(clazz);
        if (getInstancePriority(telemetryEvent, pri) < getInstancePriority(last, pri)
                || getTagPriority(telemetryEvent, pri) < getTagPriority(last, pri)) {
            accept(telemetryEvent, clazz);
        }

        // filter
    }

    private void accept(TaggedTelemetryEvent telemetryEvent, Class<? extends TaggedTelemetryEvent> clazz) {
        lastUpdates.put(clazz, telemetryEvent);
        out.send(telemetryEvent);
    }

    public static class Priorities {
        private final HashMap<Class<?>, Integer> tagPriorities = new HashMap<>();
        private final HashMap<Class<? extends TaggedTelemetryEvent>, Integer>  instancePriorities = new HashMap<>();
        private final Class<? extends TaggedTelemetryEvent> event;

        public Priorities(Class<? extends TaggedTelemetryEvent> event, List<Class<? extends TaggedTelemetryEvent>> instancePriorities, Class<?> [] tagPriorities) {
            this.event = event;
            for (int i = 0; i < instancePriorities.size(); i++) {
                this.instancePriorities.put(instancePriorities.get(i), i);
            }
            for (int i = 0; i < tagPriorities.length; i++) {
                this.tagPriorities.put(tagPriorities[i], i);
            }
        }
    }

    // prioritised event to last received example
    private HashMap<Class<? extends TaggedTelemetryEvent>, TaggedTelemetryEvent> lastUpdates = new HashMap<>();
    // prioritised event to priorities
    private HashMap<Class<? extends TaggedTelemetryEvent>, Priorities> priorities = new HashMap<>();
    // telemetry event to base class in priorities list
    private HashMap<Class<? extends TaggedTelemetryEvent>, Class<? extends TaggedTelemetryEvent>> mappings = new HashMap<>();
    // all events that we filter
    private ArrayList<Class<? extends TaggedTelemetryEvent>> allPrioritised = new ArrayList<>();

    public EventPrioritiser(final FilteredBroadcastMessenger<TaggedTelemetryEvent> out, long timeoutNanos,
                            final Priorities[] priorities) {
        for (Priorities p : priorities) {
            allPrioritised.add(p.event);
            this.priorities.put(p.event, p);
        }
        this.out = out;

    }

    private Integer getTagPriority(TaggedTelemetryEvent telemetryEvent, Priorities pri) {
        Integer result =  pri.tagPriorities.get(telemetryEvent.getTag().getClass());
        if (result == null) return Integer.MAX_VALUE;
        return result;
    }

    private Integer getInstancePriority(TaggedTelemetryEvent telemetryEvent, Priorities pri) {
        Integer result = pri.instancePriorities.get(telemetryEvent.getClass());
        if (result == null) return Integer.MAX_VALUE;
        return result;
    }
}
