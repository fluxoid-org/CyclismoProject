package org.cowboycoders.ant.profiles.common.decode.power;

import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.common.telemetry.CoastDetectedEvent;
import org.cowboycoders.ant.profiles.common.telemetry.PowerUpdate;
import org.cowboycoders.ant.profiles.common.telemetry.TelemetryEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 04/01/17.
 */
public class PowerOnlyDecoder implements org.cowboycoders.ant.profiles.common.decode.CounterBasedDecoder {

    private final BroadcastMessenger<TelemetryEvent> bus;
    private CoastDetector coastDetector = new CoastDetector();
    private PowerOnlyPage prev;

    PowerOnlyDecoder(BroadcastMessenger<TelemetryEvent> updateHub) {
        this.bus = updateHub;
    }

    public void update(PowerOnlyPage next) {
        coastDetector.update(next);
        if (!next.isValidDelta(prev) || prev == null) {
            prev = next;
            return;
        }
        if (next.getEventCountDelta(prev) == 0) {
            coastDetector.startCoast(next);
            return;
        }
        if (coastDetector.isCoasting()) {
            bus.sendMessage(new CoastDetectedEvent());
        } else {
            coastDetector.stopCoast();
            BigDecimal power =  new BigDecimal(next.getSumPowerDelta(prev))
                    .divide(new BigDecimal(next.getEventCountDelta(prev)),
                            5, RoundingMode.HALF_UP);
            bus.sendMessage(new PowerUpdate(power));
        }
    }

    @Override
    public void invalidate() {
        prev = null;
    }
}
