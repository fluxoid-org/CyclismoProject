package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.DistanceDecodable;
import org.cowboycoders.ant.profiles.common.events.DistanceUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

/**
 * Created by fluxoid on 27/01/17.
 */
public class AccDistanceDecoder<T extends DistanceDecodable> implements Decoder<T> {

    private final FilteredBroadcastMessenger<TaggedTelemetryEvent> bus;
    private long sum = 0;
    private DistanceDecodable prev;

    public AccDistanceDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
        bus = updateHub;
    }

    @Override
    public void update(T newPage) {
        if (!newPage.isDistanceAvailable()) return;
        if (prev == null) {
            prev = newPage;
            return;
        }
        sum += newPage.getDistanceDelta(prev);
        prev = newPage;
        bus.send(new DistanceUpdate(newPage, sum));
    }

    @Override
    public void invalidate() {
        prev = null;
    }

    @Override
    public void reset() {
        sum = 0;
    }
}
