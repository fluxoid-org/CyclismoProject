package org.cowboycoders.ant.profiles.events;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.events.BufferedEventPrioritiser;
import org.cowboycoders.ant.profiles.common.events.EventPrioritiser;
import org.cowboycoders.ant.profiles.common.events.EventPrioritiser.PrioritisedEvent;
import org.cowboycoders.ant.profiles.common.events.PrioritisedEventBuilder;
import org.cowboycoders.ant.profiles.common.events.SpeedUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.GeneralData;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.TorqueData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class EventPrioritiserTest {
    public static final long TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(10);
    private EventPrioritiser prioritiser;

    // classes to test instance Priorities

    private class Base extends TaggedTelemetryEvent {

        Base(Object tag) {
            super(tag);
        }
    }

    private class A extends Base {

        A(Object tag) {
            super(tag);
        }
    }

    private class B extends Base {

        B(Object tag) {
            super(tag);
        }
    }

    private class C extends Base {

        C(Object tag) {
            super(tag);
        }
    }

    // test works with deeper inheritance
    private class D extends A {

        D(Object tag) {
            super(tag);
        }
    }

    private class E extends Base {
        E(Object tag) {
            super(tag);
        }
    }

    private class Tag1 {}
    private class Tag2 {}
    private class Tag3 {}

    private Object tag1 = new Tag1();
    private Object tag2 = new Tag2();
    private Object tag3 = new Tag3();


    private FilteredBroadcastMessenger<TaggedTelemetryEvent> in;
    private FilteredBroadcastMessenger<TaggedTelemetryEvent> out;
    private PrioritisedEvent[] priorities = new PrioritisedEvent[]{
            new PrioritisedEventBuilder(Base.class)
                    .setInstancePriorities(D.class, A.class, B.class, C.class)
                    .setTagPriorities(Tag1.class, Tag2.class, Tag3.class)
                    .setTimeout(TIMEOUT_NANOS)
                    .createPrioritisedEvent(),
            new PrioritisedEventBuilder(SpeedUpdate.class)
                    .setTagPriorities(GeneralData.class, TorqueData.class)
                    .createPrioritisedEvent(),


    };

    public void initPipeline() {
        in = new FilteredBroadcastMessenger<>();
        out = new FilteredBroadcastMessenger<>();
        prioritiser = new EventPrioritiser(out, priorities);
        in.addListener(TaggedTelemetryEvent.class, prioritiser);
    }

    public void initBuffered() {
        in = new FilteredBroadcastMessenger<>();
        out = new FilteredBroadcastMessenger<>();
        EventPrioritiser bufferedPrioritiser = new BufferedEventPrioritiser(out, priorities);
        in.addListener(TaggedTelemetryEvent.class, bufferedPrioritiser);
    }

    @Test
    public void instanceTrumpsTag() {
        initPipeline();
        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
                assertTrue(A.class.isInstance(taggedTelemetryEvent));
            }
        });

        in.send(new A(tag3));
        in.send(new B(tag1)); // higher tag priority, lower instance priority

    }

    @Test
    public void lowerInstancePriorityEventsShouldBeFiltered() {
        initPipeline();
        final int [] res = new int[1];

        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
               res[0] += 1;
               assertTrue(D.class.isInstance(taggedTelemetryEvent));
            }
        });

        in.send(new D(tag1)); // highest priority
        in.send(new A(tag1)); // filtered
        in.send(new B(tag1)); // filtered
        in.send(new C(tag1)); // filtered
        in.send(new D(tag1)); // let through

        assertEquals(2, res[0]);

    }

    @Test
    public void lowerInstanceAllowedThroughBeforeHigher() {
        initPipeline();
        final ArrayList<Class<?>> res = new ArrayList<>();
        final ArrayList<Class<?>> expected = new ArrayList<>();
        expected.add(A.class);
        expected.add(D.class);
        expected.add(D.class);

        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
                res.add(taggedTelemetryEvent.getClass());
            }
        });

        in.send(new A(tag1)); // initially allowed through
        in.send(new D(tag1)); // highest priority
        in.send(new A(tag1)); // filtered
        in.send(new B(tag1)); // filtered
        in.send(new C(tag1)); // filtered
        in.send(new D(tag1)); // let through

        assertEquals(expected, res);
    }

    @Test
    public void correctlyFiltersBaseOnTag() {
        initPipeline();
        final ArrayList<Class<?>> res = new ArrayList<>();
        final ArrayList<Class<?>> expected = new ArrayList<>();
        expected.add(Tag3.class);
        expected.add(Tag2.class);
        expected.add(Tag1.class);

        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
                res.add(taggedTelemetryEvent.getTag().getClass());
            }
        });

        in.send(new A(tag3));
        in.send(new A(tag2));
        in.send(new A(tag1));
        in.send(new A(tag3));
        in.send(new A(tag2));

        assertEquals(expected, res);
    }

    @Test
    public void worksWithReal() {
        initPipeline();
        final ArrayList<Class<?>> res = new ArrayList<>();
        final ArrayList<Class<?>> expected = new ArrayList<>();
        expected.add(GeneralData.class);

        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
                res.add(taggedTelemetryEvent.getTag().getClass());
            }
        });

        in.send(new SpeedUpdate(new GeneralData(new byte[8]), null, false));
        in.send(new SpeedUpdate(new TorqueData(new byte[8]), null, false));

        assertEquals(expected, res);
    }

    @Test
    public void unprioritisedPassedThrough() {
        initPipeline();
        final int [] res = new int[1];

        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
                res[0] += 1;
                assertTrue(E.class.isInstance(taggedTelemetryEvent));
            }
        });

        in.send(new E(tag1)); // no priority set

        assertEquals(1, res[0]);
    }

    @Test
    public void msgStaleAfterTimeout() {
        initPipeline();
        in = new FilteredBroadcastMessenger<>();

        final ArrayList<Class<?>> res = new ArrayList<>();
        final ArrayList<Class<?>> expected = new ArrayList<>();
        expected.add(D.class);
        expected.add(A.class);

        EventPrioritiser spy = spy(prioritiser);
        when(spy.getTimeStamp()).
                thenReturn(0L)
        .thenReturn(TIMEOUT_NANOS + 1); // +1 so that it is strictly greater

        in.addListener(TaggedTelemetryEvent.class, spy);

        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
                res.add(taggedTelemetryEvent.getClass());
            }
        });
        in.send(new D(tag1)); // highest priority
        in.send(new A(tag1)); // lower priority, but accepted because of timeout

        assertEquals(expected, res);

    }

    @Test
    public void bufferedLowerTagsAssumedSameEvent() {
        initBuffered();
        final ArrayList<Class<?>> res = new ArrayList<>();
        final ArrayList<Class<?>> expected = new ArrayList<>();
        expected.add(A.class);

        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
                res.add(taggedTelemetryEvent.getClass());
            }
        });

        in.send(new A(tag2));
        assertEquals(expected, res);
        in.send(new A(tag1)); // assumed to same event as previous
        assertEquals(expected, res);

        expected.add(A.class);
        in.send(new A(tag1));
        assertEquals(expected,res);
    }

    @Test
    public void bufferedFiltersWhenEventLockedIn() {
        initBuffered();
        final ArrayList<Class<?>> res = new ArrayList<>();
        final ArrayList<Class<?>> expected = new ArrayList<>();
        expected.add(A.class);

        out.addListener(TaggedTelemetryEvent.class, new BroadcastListener<TaggedTelemetryEvent>() {
            @Override
            public void receiveMessage(TaggedTelemetryEvent taggedTelemetryEvent) {
                res.add(taggedTelemetryEvent.getClass());
            }
        });

        in.send(new A(tag3));
        assertEquals(expected, res);

        in.send(new A(tag1));
        assertEquals(expected,res);

        in.send(new A(tag2));
        assertEquals(expected, res);

        expected.add(A.class);
        in.send(new A(tag1));
    }


}
