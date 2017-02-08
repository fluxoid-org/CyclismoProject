package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.SpeedDecodable;
import org.cowboycoders.ant.profiles.common.decode.utils.CounterBasedDecoder;
import org.cowboycoders.ant.profiles.common.events.WheelFreqUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TelemetryEvent;

;import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Created by fluxoid on 10/01/17.
 */
public class SpeedDecoder implements Decoder<SpeedDecodable> {


    // otherwise gives you km/h when you times by circumference
    private final BigDecimal convertToPerSecond = new BigDecimal(3.6);

    private long rotationPeriodDelta;
    private MyCounterBasedDecoder decoder;

    public SpeedDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        decoder = new MyCounterBasedDecoder(updateHub);
        reset();
    }

    public void reset() {
        rotationPeriodDelta = 0;
    }

    private class MyCounterBasedDecoder extends CounterBasedDecoder<SpeedDecodable> {

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
            bus.sendMessage(new WheelFreqUpdate(freq));
        }
    }


    @Override
    public void update(SpeedDecodable newPage) {
        decoder.update(newPage);
    }

    @Override
    public void invalidate() {
        decoder.invalidate();
    }
}
