package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.events.SpeedUpdate;
import org.cowboycoders.ant.profiles.common.events.WheelFreqUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.TorqueData;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.assertEquals;

/**
 * Created by fluxoid on 07/02/17.
 */
public class SpeedDecoderTest {


    private static final BigDecimal WHEEL_CIRCUM = new BigDecimal(0.7); //m

    @Test
    public void matchesKnownGood() {
        FilteredBroadcastMessenger<TaggedTelemetryEvent> bus = new FilteredBroadcastMessenger<TaggedTelemetryEvent>();
        class FreqListener implements BroadcastListener<TaggedTelemetryEvent>  {
            private BigDecimal freq;
            private BigDecimal speedKmh;
            public void receiveMessage(TaggedTelemetryEvent telemetryEvent) {
                if (telemetryEvent instanceof WheelFreqUpdate) {
                    WheelFreqUpdate up = (WheelFreqUpdate) telemetryEvent;
                    freq = up.getRotationalFrequency();
                }
                if (telemetryEvent instanceof SpeedUpdate) {
                    speedKmh = ((SpeedUpdate) telemetryEvent).getSpeed();
                }
            }
        };
        FreqListener freqListener = new FreqListener();
        bus.addListener(TaggedTelemetryEvent.class, freqListener);
        BigDecimal speed = new BigDecimal(10.0); // metres / per second
        BigDecimal period = WHEEL_CIRCUM.divide(speed, 20, BigDecimal.ROUND_HALF_UP);
        int power = 200;
        int rotationsDelta = 10;
        int eventsDelta = 1;
        final byte[] data1 = new byte[8];
        final byte[] data2 = new byte[8];

        new TorqueData.TorqueDataPayload()
                .encode(data1);

        new TorqueData.TorqueDataPayload()
                .setEvents(eventsDelta)
                .updateTorqueSumFromPower(power, period)
                .setRotations(rotationsDelta)
                .encode(data2);

        TorqueData p1 = new TorqueData(data1);
        TorqueData p2 = new TorqueData(data2);

        SpeedDecoder dec = new SpeedDecoder(bus, WHEEL_CIRCUM);
        dec.update(p1);
        dec.update(p2);
        // there is some rounding error in decode / encode step

        BigDecimal calcSpeed = WHEEL_CIRCUM.multiply(freqListener.freq);
        assertEquals(
                speed.setScale(1, RoundingMode.HALF_UP),
                calcSpeed.setScale(1, RoundingMode.HALF_UP)
                );
        assertEquals(
                speed.multiply(new BigDecimal(3.6)).setScale(0, RoundingMode.HALF_UP), // km/h conversion
                freqListener.speedKmh.setScale(0, RoundingMode.HALF_UP)
        );

    }

}

