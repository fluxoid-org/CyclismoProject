package org.cowboycoders.ant.profiles.common.events.interfaces;

/**
 * Created by fluxoid on 05/01/17.
 */
public abstract class TaggedTelemetryEvent implements TelemetryEvent {

    private final Object tag;

    protected TaggedTelemetryEvent(Object tag) {
        this.tag = tag;
    }
    /**
     *
     * @return arbitrary tag describing data source
     */
    @Override
    public Object getTag() {
        return this.tag;
    }
}
