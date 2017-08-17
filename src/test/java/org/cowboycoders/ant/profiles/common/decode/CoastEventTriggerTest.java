package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CounterBasedDecodable;
import org.cowboycoders.ant.profiles.common.decode.interfaces.PowerOnlyDecodable;
import org.cowboycoders.ant.profiles.common.decode.utils.CoastDetector;
import org.cowboycoders.ant.profiles.common.events.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CoastEventTriggerTest {
    /**
     * should detect coast when timestamp delta >= COAST_WINDOW and eventDelta = 0
     */
    @Test
    public void testCoastTimeout() {
        class CoastHelper implements BroadcastListener<TaggedTelemetryEvent> {
            boolean coastDetected = false;

            public void receiveMessage(TaggedTelemetryEvent event) {
                if (!(event instanceof CoastDetectedEvent)) {
                    return;
                }
                coastDetected = true;
            }
        }

        FilteredBroadcastMessenger<TaggedTelemetryEvent> bus = new FilteredBroadcastMessenger<TaggedTelemetryEvent>();
        CoastEventTrigger<PowerOnlyDecodable> decoder = new CoastEventTrigger<>(bus);

        CoastHelper listener = new CoastHelper();
        bus.addListener(TaggedTelemetryEvent.class, listener);

        decoder.update(new PowerOnlyDecodable() {
            public long getSumPowerDelta(PowerOnlyDecodable old) {
                return 0;
            }

            public long getEventCountDelta(CounterBasedDecodable old) {
                return 0;
            }

            public int getSumPower() {
                return 0;
            }

            public int getEventCount() {
                return 0;
            }

            public boolean isValidDelta(CounterBasedDecodable old) {
                return true;
            }

            public int getInstantPower() {
                return 0;
            }

            public long getTimestamp() {
                return 0;
            }
        });
        decoder.update(new PowerOnlyDecodable() {
            public long getSumPowerDelta(PowerOnlyDecodable old) {
                return 0;
            }

            public long getEventCountDelta(CounterBasedDecodable old) {
                // must be zero to trigger coast detection
                return 0;
            }

            public int getSumPower() {
                return 0;
            }

            public int getEventCount() {
                return 0;
            }

            public boolean isValidDelta(CounterBasedDecodable old) {
                return true;
            }

            public int getInstantPower() {
                return 0;
            }

            public long getTimestamp() {
                return CoastDetector.COAST_WINDOW;
            }
        });

        assertTrue(listener.coastDetected);
    }
}
