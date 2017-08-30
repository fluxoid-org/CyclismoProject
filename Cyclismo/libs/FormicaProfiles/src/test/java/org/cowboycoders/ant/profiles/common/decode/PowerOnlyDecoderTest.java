package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.utils.CoastDetector;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CounterBasedDecodable;
import org.cowboycoders.ant.profiles.common.decode.interfaces.PowerOnlyDecodable;
import org.cowboycoders.ant.profiles.common.events.AveragePowerUpdate;
import org.cowboycoders.ant.profiles.common.events.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by fluxoid on 06/01/17.
 */
public class PowerOnlyDecoderTest {

    /**
     * Tests averaged freq = (summation of freq readings) / (number of freq readings in summation)
     */
    @Test
    public void testSumPower() {
        final double SIM_POWER = 123.0;
        final long EVENTS = 100;
        class PowerSumListener implements BroadcastListener<TaggedTelemetryEvent> {
            BigDecimal powersum = null;
            long events = 0;
            long sum = 0;

            public void receiveMessage(TaggedTelemetryEvent event) {
                if (!(event instanceof AveragePowerUpdate)) {
                    return;
                }
                AveragePowerUpdate casted = (AveragePowerUpdate) event;
                powersum = casted.getAveragePower();
                events = casted.getEvents();
                sum = casted.getSumPower();
            }
        }

        FilteredBroadcastMessenger<TaggedTelemetryEvent> bus = new FilteredBroadcastMessenger<TaggedTelemetryEvent>();
        PowerOnlyDecoder<PowerOnlyDecodable> decoder = new PowerOnlyDecoder<>(bus);

        PowerSumListener listener = new PowerSumListener();
        bus.addListener(TaggedTelemetryEvent.class, listener);

        decoder.update(new PowerOnlyDecodable() {
            public long getSumPowerDelta(PowerOnlyDecodable old) {
                //ignored
                return 0;
            }

            public long getEventCountDelta(CounterBasedDecodable old) {
                //ignored
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
                return (long) (EVENTS * SIM_POWER);
            }

            public long getEventCountDelta(CounterBasedDecodable old) {
                return EVENTS;
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
                return 1;
            }
        });
        assertEquals(listener.powersum.doubleValue(), SIM_POWER, 0.1);
        assertEquals(listener.events, EVENTS);
        assertEquals(listener.sum, (long) (EVENTS * SIM_POWER));
    }
}
