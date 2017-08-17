package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CounterBasedDecodable;
import org.cowboycoders.ant.profiles.common.decode.utils.CounterBasedDecoder;
import org.cowboycoders.ant.profiles.common.events.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.events.CoastEndEvent;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

public class CoastEventTrigger<T extends CounterBasedDecodable> implements Decoder<T> {

    private final CoastEventTrigger.CoastHelper helper;

    public CoastEventTrigger(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
        helper = new CoastHelper(updateHub);
    }

    private class CoastHelper extends CounterBasedDecoder<T> {

        public CoastHelper(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
            super(updateHub);
        }

        @Override
        protected void onInitializeCounters() {

        }

        @Override
        protected void onValidDelta() {

        }

        @Override
        protected void onNoCoast() {

        }

        @Override
        protected void onUpdate() {

        }

        @Override
        protected void onCoastStart() {
            bus.send(new CoastDetectedEvent(getCurrentPage().getClass()));
        }

        @Override
        protected void onCoastStop() {
            bus.send(new CoastEndEvent(getCurrentPage().getClass()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(T newPage) {
        helper.update(newPage);
    }

    @Override
    public void invalidate() {
        helper.invalidate();
    }

    @Override
    public void reset() {
        helper.invalidate();
    }
}
