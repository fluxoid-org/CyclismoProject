package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.LapFlagDecodable;
import org.cowboycoders.ant.profiles.common.events.LapUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

public class LapFlagDecoder<T extends LapFlagDecodable> implements Decoder<T> {

    private final FilteredBroadcastMessenger<TaggedTelemetryEvent> bus;
    private int laps = 0;
    private LapFlagDecodable prev;

    public LapFlagDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> bus) {
        this.bus = bus;
    }


    @Override
    public void update(T newPage) {
        if (prev == null) {prev = newPage; return;}
        final boolean prevState = prev.isLapToggled();
        final boolean state = newPage.isLapToggled();
        prev = newPage;
        if (state == prevState) return;
        laps += 1;
        bus.send(new LapUpdate(newPage,laps));

    }

    @Override
    public void invalidate() {
        prev = null;
    }

    @Override
    public void reset() {
        laps = 0;
        invalidate();
    }
}
