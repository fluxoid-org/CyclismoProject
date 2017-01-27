package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.TimeDecodable;
import org.cowboycoders.ant.profiles.common.events.TimeElapsedUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

import java.math.BigDecimal;

/**
 * Created by fluxoid on 27/01/17.
 */
public class TimeDecoder implements Decoder<TimeDecodable> {

    private final BroadcastMessenger<TelemetryEvent> bus;
    private long timeSum = 0;
    private TimeDecodable prev;

    public TimeDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        bus = updateHub;
    }

    @Override
    public void update(TimeDecodable newPage) {
        if (prev == null) {
            prev = newPage;
            return;
        }
        timeSum += newPage.getTicksDelta(prev);
        BigDecimal seconds = newPage.ticksToSeconds(timeSum);
        bus.sendMessage(new TimeElapsedUpdate(seconds));
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
