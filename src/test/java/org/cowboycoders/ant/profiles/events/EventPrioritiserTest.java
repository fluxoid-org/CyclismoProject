package org.cowboycoders.ant.profiles.events;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.events.EventPrioritiser;
import org.cowboycoders.ant.profiles.common.events.GenericPowerUpdate;
import org.cowboycoders.ant.profiles.common.events.InstantPowerUpdate;
import org.cowboycoders.ant.profiles.common.events.TorquePowerUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class EventPrioritiserTest {

    @Test
    public void minimalExampleShouldFunction() {
        FilteredBroadcastMessenger<TaggedTelemetryEvent> in = new FilteredBroadcastMessenger<>();
        FilteredBroadcastMessenger<TaggedTelemetryEvent> out = new FilteredBroadcastMessenger<>();

        List<Class<? extends TaggedTelemetryEvent>> instancePriorities = new ArrayList<>();
        instancePriorities.add(TorquePowerUpdate.class);
        instancePriorities.add(InstantPowerUpdate.class);

        EventPrioritiser prioritiser = new EventPrioritiser(out, TimeUnit.SECONDS.toNanos(10), new EventPrioritiser.Priorities[] {
                new EventPrioritiser.Priorities(GenericPowerUpdate.class,
                        instancePriorities,
                        new Class<?>[0]),
        });

        in.addListener(TaggedTelemetryEvent.class, prioritiser);

        final int [] res = new int[1];

        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
               res[0] += 1;
            }
        });

        sendInstantPower(in);
        sendTorquePower(in);
        sendInstantPower(in);
        sendInstantPower(in);

        assertEquals(2, res[0]);

    }

    private void sendTorquePower(FilteredBroadcastMessenger<TaggedTelemetryEvent> in) {
        in.send(new TorquePowerUpdate(new Object(), 2000,2));
    }

    private void sendInstantPower(FilteredBroadcastMessenger<TaggedTelemetryEvent> in) {
        in.send(new InstantPowerUpdate(new Object(), new BigDecimal(0.0)));
    }
}
