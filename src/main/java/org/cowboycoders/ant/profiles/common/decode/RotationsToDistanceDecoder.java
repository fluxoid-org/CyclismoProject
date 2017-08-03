package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.RotationsToDistanceDecodable;
import org.cowboycoders.ant.profiles.common.decode.utils.CounterBasedDecoder;;
import org.cowboycoders.ant.profiles.common.events.DistanceUpdate;
import org.cowboycoders.ant.profiles.common.events.WheelRotationsUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

import java.math.BigDecimal;


/**
 * Created by fluxoid on 10/01/17.
 */
public class RotationsToDistanceDecoder implements Decoder<RotationsToDistanceDecodable> {


    private final BigDecimal wheelCircumferece;
    private long wheelTicks;
    private MyCounterBasedDecoder decoder;

    public RotationsToDistanceDecoder(FilteredBroadcastMessenger<TelemetryEvent> updateHub, BigDecimal wheelCircumference) {
        assert  wheelCircumference != null;
        this.wheelCircumferece = wheelCircumference;
        decoder = new MyCounterBasedDecoder(updateHub);
        reset();
    }

    public void reset() {
        wheelTicks = 0;
    }

    private class MyCounterBasedDecoder extends CounterBasedDecoder<RotationsToDistanceDecodable> {

        public MyCounterBasedDecoder(FilteredBroadcastMessenger<TelemetryEvent> updateHub) {
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
            RotationsToDistanceDecodable next = getCurrentPage();
            RotationsToDistanceDecodable prev = getPreviousPage();
            wheelTicks += next.getWheelRotationsDelta(prev);
        }

        @Override
        protected void onNoCoast() {
            bus.send(new WheelRotationsUpdate(wheelTicks));
            bus.send(new DistanceUpdate(wheelCircumferece.multiply(new BigDecimal(wheelTicks))));
        }
    }


    @Override
    public void update(RotationsToDistanceDecodable newPage) {
        decoder.update(newPage);
    }

    @Override
    public void invalidate() {
        decoder.invalidate();
    }
}
