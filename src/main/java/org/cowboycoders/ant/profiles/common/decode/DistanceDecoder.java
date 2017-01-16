package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.DistanceDecodable;
import org.cowboycoders.ant.profiles.common.decode.utils.CounterBasedDecoder;;
import org.cowboycoders.ant.profiles.common.events.WheelRotationsUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;


/**
 * Created by fluxoid on 10/01/17.
 */
public class DistanceDecoder implements Decoder<DistanceDecodable> {


    private long wheelTicks;
    private MyCounterBasedDecoder decoder;

    public DistanceDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        decoder = new MyCounterBasedDecoder(updateHub);
        reset();
    }

    public void reset() {
        wheelTicks = 0;
    }

    private class MyCounterBasedDecoder extends CounterBasedDecoder<DistanceDecodable> {

        public MyCounterBasedDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
            super(updateHub);
        }


        @Override
        protected void onUpdate() {
        }

        @Override
        protected void onInitializeCounters() {

        }

        @Override
        protected void onValidDelta() {
            DistanceDecodable next = getCurrentPage();
            DistanceDecodable prev = getPreviousPage();
            wheelTicks += next.getWheelRotationsDelta(prev);
        }

        @Override
        protected void onNoCoast() {
            bus.sendMessage(new WheelRotationsUpdate(wheelTicks));
        }
    }


    @Override
    public void update(DistanceDecodable newPage) {
        decoder.update(newPage);
    }

    @Override
    public void invalidate() {
        decoder.invalidate();
    }
}
