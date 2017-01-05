package org.cowboycoders.ant.profiles.common.decode.power;

import org.cowboycoders.ant.profiles.common.AbstractTelemetryData;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Created by fluxoid on 04/01/17.
 */
public abstract class PowerDecodingBehaviour {

    private long coastStart;
    private boolean coastWindowOpen;
    private static final long COAST_WINDOW = TimeUnit.MILLISECONDS.toNanos(2875);

    public AbstractPowerData getLatest() {
        return latest;
    }

    public AbstractPowerData getPrevious() {
        return previous;
    }

    private AbstractPowerData latest;
    private AbstractPowerData previous;

    public void update(AbstractPowerData param) {
        previous = latest;
        latest = param;
        onUpdate(param);
    }

    public abstract void onUpdate(AbstractPowerData param);

    public boolean isCoasting() {
        if (coastWindowOpen) {
            if (getLatestStamp() - coastStart >= COAST_WINDOW) {
                return true;
            }
        }
        return false;
    }

    public abstract BigDecimal getPower();

    protected long getLatestStamp() {
        return latest.getTimestamp();
    }

    public void startCoast(AbstractTelemetryData start) {
        coastWindowOpen = true;
        coastStart = start.getTimestamp();
    }

    public void stopCoast() {
        coastWindowOpen = false;
    }
}
