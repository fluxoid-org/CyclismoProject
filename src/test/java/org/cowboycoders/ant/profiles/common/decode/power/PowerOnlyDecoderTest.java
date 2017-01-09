package org.cowboycoders.ant.profiles.common.decode.power;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.CoastDetector;
import org.cowboycoders.ant.profiles.common.decode.CounterBasedPage;
import org.cowboycoders.ant.profiles.common.telemetry.AveragedPowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.telemetry.TelemetryEvent;
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

        BroadcastMessenger<TelemetryEvent> bus = new BroadcastMessenger();
        PowerOnlyDecoder decoder = new PowerOnlyDecoder(bus);

        CoastHelper listener = new CoastHelper();
        bus.addBroadcastListener(listener);

        decoder.update(new PowerOnlyPage() {
            public long getSumPowerDelta(PowerOnlyPage old) {
                return 0;
            }

            public long getEventCountDelta(CounterBasedPage old) {
                return 0;
            }

            public int getSumPower() {
                return 0;
            }

            public int getEventCount() {
                return 0;
            }

            public boolean isValidDelta(PowerOnlyPage old) {
                return true;
            }

            public int getInstantPower() {
                return 0;
            }

            public long getTimestamp() {
                return 0;
            }
        });
        decoder.update(new PowerOnlyPage() {
            public long getSumPowerDelta(PowerOnlyPage old) {
                return 0;
            }

            public long getEventCountDelta(CounterBasedPage old) {
                // must be zero to trigger coast detection
                return 0;
            }

            public int getSumPower() {
                return 0;
            }

            public int getEventCount() {
                return 0;
            }

            public boolean isValidDelta(PowerOnlyPage old) {
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
     * Tests averaged power = (summation of power readings) / (number of power readings in summation)
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
                powersum = casted.getPower();
                events = casted.getEvents();
                sum = casted.getSumPower();
            }
        }

        BroadcastMessenger<TelemetryEvent> bus = new BroadcastMessenger();
        PowerOnlyDecoder decoder = new PowerOnlyDecoder(bus);

        PowerSumListener listener = new PowerSumListener();
        bus.addBroadcastListener(listener);

        decoder.update(new PowerOnlyPage() {
            public long getSumPowerDelta(PowerOnlyPage old) {
                //ignored
                return 0;
            }

            public long getEventCountDelta(CounterBasedPage old) {
                //ignored
                return 0;
            }

            public int getSumPower() {
                return 0;
            }

            public int getEventCount() {
                return 0;
            }

            public boolean isValidDelta(PowerOnlyPage old) {
                return true;
            }

            public int getInstantPower() {
                return 0;
            }

            public long getTimestamp() {
                return 0;
            }
        });
        decoder.update(new PowerOnlyPage() {
            public long getSumPowerDelta(PowerOnlyPage old) {
                return (long) (EVENTS * SIM_POWER);
            }

            public long getEventCountDelta(CounterBasedPage old) {
                return EVENTS;
            }

            public int getSumPower() {
                return 0;
            }

            public int getEventCount() {
                return 0;
            }

            public boolean isValidDelta(PowerOnlyPage old) {
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
