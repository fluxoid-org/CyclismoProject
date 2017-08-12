package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.TorqueDecodable;
import org.cowboycoders.ant.profiles.common.events.TorquePowerUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.TorqueData;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by fluxoid on 07/02/17.
 */
public class TorqueDecoderTest {


    private static final BigDecimal WHEEL_CIRCUM = new BigDecimal(700); //mm

    @Test
    public void matchesKnownGood() {
        FilteredBroadcastMessenger<TaggedTelemetryEvent> bus = new FilteredBroadcastMessenger<TaggedTelemetryEvent>();
        class PowerListener implements BroadcastListener<TaggedTelemetryEvent>  {
            BigDecimal power;
            public void receiveMessage(TaggedTelemetryEvent telemetryEvent) {
                if (telemetryEvent instanceof TorquePowerUpdate) {
                    TorquePowerUpdate up = (TorquePowerUpdate) telemetryEvent;
                    power = up.getPower();
                }
            }
        };
        PowerListener powerListener = new PowerListener();
        bus.addListener(TaggedTelemetryEvent.class, powerListener);
        BigDecimal speed = new BigDecimal(10.0);
        BigDecimal period = WHEEL_CIRCUM.multiply(new BigDecimal(Math.pow(10, -3))).divide(speed, 20, BigDecimal.ROUND_HALF_UP);
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

        TorqueDecoder<TorqueDecodable> dec = new TorqueDecoder<>(bus);
        dec.update(p1);
        dec.update(p2);
        // there is some rounding error in decode / encode step
        assertEquals(new BigDecimal(power).setScale(0, RoundingMode.HALF_UP),
                powerListener.power.setScale(0, RoundingMode.HALF_UP));

    }

}

