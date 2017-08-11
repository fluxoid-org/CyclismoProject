package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.SpeedDecodable;
import org.cowboycoders.ant.profiles.common.decode.utils.CounterBasedDecoder;
import org.cowboycoders.ant.profiles.common.events.SpeedUpdate;
import org.cowboycoders.ant.profiles.common.events.WheelFreqUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

;import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Created by fluxoid on 10/01/17.
 */
public class SpeedDecoder<T extends SpeedDecodable> implements Decoder<T> {


    // otherwise gives you km/h when you times by circumference
    private final BigDecimal convertToPerSecond = new BigDecimal(3.6);
    private final BigDecimal wheelCircumferece;

    private long rotationPeriodDelta;
    private MyCounterBasedDecoder decoder;

    public SpeedDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub, BigDecimal wheelCircumference) {
        assert wheelCircumference != null;
        decoder = new MyCounterBasedDecoder(updateHub);
        this.wheelCircumferece = wheelCircumference;
        reset();
    }

    public void reset() {
        rotationPeriodDelta = 0;
    }

    private class MyCounterBasedDecoder extends CounterBasedDecoder<SpeedDecodable> {

        public MyCounterBasedDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
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
            SpeedDecodable next = getCurrentPage();
            SpeedDecodable prev = getPreviousPage();
            rotationPeriodDelta = next.getRotationPeriodDelta(prev);
        }

        @Override
        protected void onNoCoast() {
            // 73728 = 2048 * 10 * 3.6
            // actual rotationPeriod = rotationPeriodDelta / 2048
            // ie we could get rid of convertToPerSecond,
            BigDecimal freq = new BigDecimal(2048).multiply(new BigDecimal(this.getEventDelta())
                    .divide(new BigDecimal(rotationPeriodDelta), 4, RoundingMode.HALF_UP));
            bus.send(new WheelFreqUpdate(getCurrentPage().getClass() ,freq));
            bus.send(new SpeedUpdate(getCurrentPage().getClass(), freq.multiply(wheelCircumferece)
                    // convert to kh/h from m/s
                    .multiply(new BigDecimal (3.6)).setScale(2, RoundingMode.HALF_UP), false));
        }
    }


    @Override
    public void update(T newPage) {
        decoder.update(newPage);
    }

    @Override
    public void invalidate() {
        decoder.invalidate();
    }
}
