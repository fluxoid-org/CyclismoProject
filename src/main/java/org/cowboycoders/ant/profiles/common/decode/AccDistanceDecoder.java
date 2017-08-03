package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.DistanceDecodable;
import org.cowboycoders.ant.profiles.common.events.DistanceUpdate;
import org.cowboycoders.ant.profiles.common.events.TimeElapsedUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

/**
 * Created by fluxoid on 27/01/17.
 */
public class AccDistanceDecoder implements Decoder<DistanceDecodable> {

    private final FilteredBroadcastMessenger<TelemetryEvent> bus;
    private long sum = 0;
    private DistanceDecodable prev;

    public AccDistanceDecoder(FilteredBroadcastMessenger<TelemetryEvent> updateHub) {
        bus = updateHub;
    }

    @Override
    public void update(DistanceDecodable newPage) {
        if (!newPage.isDistanceAvailable()) return;
        if (prev == null) {
            prev = newPage;
            return;
        }
        sum += newPage.getDistanceDelta(prev);
        prev = newPage;
        bus.send(new DistanceUpdate(sum));
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
