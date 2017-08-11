package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.DistanceDecodable;
import org.cowboycoders.ant.profiles.common.events.DistanceUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.cowboycoders.ant.profiles.common.utils.CounterUtils;
import org.junit.Test;

import java.math.BigDecimal;

import static org.cowboycoders.ant.profiles.BitManipulation.UNSIGNED_INT8_MAX;
import static org.junit.Assert.assertEquals;

abstract class DistanceDecodablePartial implements DistanceDecodable {
    @Override
    public long getDistanceDelta(DistanceDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT8_MAX, old.getDistanceCovered(), getDistanceCovered());
    }
}

public class DistanceDecoderTest {

    private DistanceDecodable a = new DistanceDecodablePartial() {

        @Override
        public Integer getDistanceCovered() {
            return 200;
        }

        @Override
        public boolean isDistanceAvailable() {
            return true;
        }
    };

    private DistanceDecodable b = new DistanceDecodablePartial() {

        @Override
        public Integer getDistanceCovered() {
            return 9;
        }

        @Override
        public boolean isDistanceAvailable() {
            return true;
        }
    };



    @Test
    public void matchesKnownGood() {



        final FilteredBroadcastMessenger<TaggedTelemetryEvent> bus = new FilteredBroadcastMessenger<>();
        AccDistanceDecoder dist = new AccDistanceDecoder(bus);
        final BigDecimal expected = new BigDecimal(65);
        final BigDecimal[] actual = {null};
        bus.addListener(DistanceUpdate.class, new BroadcastListener<DistanceUpdate>() {
            @Override
            public void receiveMessage(DistanceUpdate distanceUpdate) {
                actual[0] = distanceUpdate.getDistance();
            }
        });

        dist.update(a);
        dist.update(b);
        assertEquals(expected, actual[0]);
    }

    @Test
    public void doubleDecode() {



        final FilteredBroadcastMessenger<TaggedTelemetryEvent> bus = new FilteredBroadcastMessenger<>();
        AccDistanceDecoder dist = new AccDistanceDecoder(bus);
        final BigDecimal expected = new BigDecimal(65);
        final BigDecimal[] actual = {null};
        bus.addListener(DistanceUpdate.class, new BroadcastListener<DistanceUpdate>() {
            @Override
            public void receiveMessage(DistanceUpdate distanceUpdate) {
                actual[0] = distanceUpdate.getDistance();
            }
        });

        dist.update(a);
        dist.update(b);
        dist.update(b); // intentional (should remain the same)
        assertEquals(expected, actual[0]);
    }
}
