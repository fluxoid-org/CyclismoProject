package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.LapFlagDecodable;
import org.cowboycoders.ant.profiles.common.events.LapUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LapFlagDecoderTest {

    private final LapFlagDecodable untoggled = new LapFlagDecodable() {
        @Override
        public boolean isLapToggled() {
            return false;
        }
    };

    private final LapFlagDecodable toggled = new LapFlagDecodable() {
        @Override
        public boolean isLapToggled() {
            return true;
        }
    };

    @Test
    public void shouldIncrement() {
        final FilteredBroadcastMessenger<TelemetryEvent> bus = new FilteredBroadcastMessenger<>();
        final LapFlagDecoder decoder = new LapFlagDecoder(bus);
        final int[] res = new int[1];
        bus.addListener(LapUpdate.class, new BroadcastListener<LapUpdate>() {
            @Override
            public void receiveMessage(LapUpdate lapUpdate) {
                res[0] = lapUpdate.getLaps();
            }
        });
        assertEquals(0, res[0]);
        decoder.update(toggled);
        assertEquals(0, res[0]);
        decoder.update(untoggled);
        assertEquals(1, res[0]);
        decoder.update(untoggled);
        assertEquals(1, res[0]);
        decoder.update(toggled);
        assertEquals(2, res[0]);

    }
}
