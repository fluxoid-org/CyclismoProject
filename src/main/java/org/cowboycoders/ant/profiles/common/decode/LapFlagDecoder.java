package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.LapFlagDecodable;
import org.cowboycoders.ant.profiles.common.events.LapUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

public class LapFlagDecoder implements Decoder<LapFlagDecodable> {

    private final FilteredBroadcastMessenger<TelemetryEvent> bus;
    private int laps = 0;
    private LapFlagDecodable prev;

    public LapFlagDecoder(FilteredBroadcastMessenger<TelemetryEvent> bus) {
        this.bus = bus;
    }


    @Override
    public void update(LapFlagDecodable newPage) {
        if (prev == null) {prev = newPage; return;}
        final boolean prevState = prev.isLapToggled();
        final boolean state = newPage.isLapToggled();
        prev = newPage;
        if (state == prevState) return;
        laps += 1;
        bus.send(new LapUpdate(laps));

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
