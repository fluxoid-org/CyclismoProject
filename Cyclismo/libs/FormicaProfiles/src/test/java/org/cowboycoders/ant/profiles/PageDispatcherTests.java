package org.cowboycoders.ant.profiles;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.BikeData;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by fluxoid on 30/01/17.
 */
public class PageDispatcherTests {

    public static org.cowboycoders.ant.profiles.common.PageDispatcher getBus() {
        return new org.cowboycoders.ant.profiles.common.PageDispatcher();
    }


    @Test
    public void dispatchesBikeData() {

        final org.cowboycoders.ant.profiles.common.PageDispatcher bus = getBus();
        class Listener implements BroadcastListener<BikeData> {
            boolean received = false;
            public void receiveMessage(BikeData bikeData) {
                received = true;
            }
        };
        Listener listener = new Listener();
        bus.addListener(BikeData.class, listener);
        byte [] data = new byte[8];
        new BikeData.BikeDataPayload()
                .encode(data);
        bus.dispatch(data);
        assertTrue(listener.received);
    }
}
