package org.cowboycoders.ant.profiles.common.events;

import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

public class PrioritisedEventBuilder {
    private Class<? extends TaggedTelemetryEvent> event;
    private Class<?>[] tagPriorities = new Class<?>[0];

    @SuppressWarnings("unchecked") // zero length
    private Class<? extends TaggedTelemetryEvent>[] instancePriorities =
            (Class<? extends TaggedTelemetryEvent>[]) new Class[0];

    public PrioritisedEventBuilder(Class<? extends TaggedTelemetryEvent> event) {
        this.event = event;
    }

    public PrioritisedEventBuilder setTagPriorities(Class<?> ... tagPriorities) {
        this.tagPriorities = tagPriorities;
        return this;
    }

    @SafeVarargs
    public final PrioritisedEventBuilder setInstancePriorities(Class<? extends TaggedTelemetryEvent>... instancePriorities) {
        this.instancePriorities = instancePriorities;
        return this;
    }

    public EventPrioritiser.PrioritisedEvent createPrioritisedEvent() {
        return new EventPrioritiser.PrioritisedEvent(event, tagPriorities, instancePriorities);
    }
}