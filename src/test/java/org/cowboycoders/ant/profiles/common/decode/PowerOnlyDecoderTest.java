package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.utils.CoastDetector;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CounterBasedDecodable;
import org.cowboycoders.ant.profiles.common.decode.interfaces.PowerOnlyDecodable;
import org.cowboycoders.ant.profiles.common.events.AveragedPowerUpdate;
import org.cowboycoders.ant.profiles.common.events.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by fluxoid on 06/01/17.
 */
public class PowerOnlyDecoderTest {


    /**
     * should detect coast when timestamp delta >= COAST_WINDOW and eventDelta = 0
     */
    @Test
    public void testCoastTimeout() {
        class CoastHelper implements BroadcastListener<TelemetryEvent> {
            boolean coastDetected = false;

            public void receiveMessage(TelemetryEvent event) {
                if (!(event instanceof CoastDetectedEvent)) {
                    return;
                }
                coastDetected = true;
            }
        }

        BroadcastMessenger<TelemetryEvent> bus = new BroadcastMessenger<TelemetryEvent>();
        PowerOnlyDecoder decoder = new PowerOnlyDecoder(bus);

        CoastHelper listener = new CoastHelper();
        bus.addBroadcastListener(listener);

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

    /**
     * Tests averaged freq = (summation of freq readings) / (number of freq readings in summation)
     */
    @Test
    public void testSumPower() {
        final double SIM_POWER = 123.0;
        final long EVENTS = 100;
        class PowerSumListener implements BroadcastListener<TelemetryEvent> {
            BigDecimal powersum = null;
            long events = 0;
            long sum = 0;

            public void receiveMessage(TelemetryEvent event) {
                if (!(event instanceof AveragedPowerUpdate)) {
                    return;
                }
                AveragedPowerUpdate casted = (AveragedPowerUpdate) event;
                powersum = casted.getAveragePower();
                events = casted.getEvents();
                sum = casted.getSumPower();
            }
        }

        BroadcastMessenger<TelemetryEvent> bus = new BroadcastMessenger<TelemetryEvent>();
        PowerOnlyDecoder decoder = new PowerOnlyDecoder(bus);

        PowerSumListener listener = new PowerSumListener();
        bus.addBroadcastListener(listener);

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
