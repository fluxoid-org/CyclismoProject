package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.TimeDecodable;
import org.cowboycoders.ant.profiles.common.events.TimeElapsedUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 27/01/17.
 */
public class TimeDecoder<T extends TimeDecodable> implements Decoder<T> {

    private final FilteredBroadcastMessenger<TaggedTelemetryEvent> bus;
    private long timeSum = 0;
    private TimeDecodable prev;

    public TimeDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
        bus = updateHub;
    }

    @Override
    public void update(T newPage) {
        if (prev == null) {
            prev = newPage;
            return;
        }
        timeSum += newPage.getTicksDelta(prev);
        BigDecimal seconds = newPage.ticksToSeconds(timeSum);
        prev = newPage;
        bus.send(new TimeElapsedUpdate(newPage ,seconds));
    }

    @Override
    public void invalidate() {
        prev = null;
    }

    @Override
    public void reset() {
        timeSum = 0;
    }
}
