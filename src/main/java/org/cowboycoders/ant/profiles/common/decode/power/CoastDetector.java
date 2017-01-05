package org.cowboycoders.ant.profiles.common.decode.power;

import org.cowboycoders.ant.profiles.common.TelemetryPage;

import java.util.concurrent.TimeUnit;

/**
 * Created by fluxoid on 04/01/17.
 */
public class CoastDetector {

    private long coastStart;
    private boolean coastWindowOpen;
    private static final long COAST_WINDOW = TimeUnit.MILLISECONDS.toNanos(2875);

    private TelemetryPage latest;
    private TelemetryPage previous;

    public void update(TelemetryPage param) {
        previous = latest;
        latest = param;
    }

    public boolean isCoasting() {
        if (coastWindowOpen) {
            if (getLatestStamp() - coastStart >= COAST_WINDOW) {
                return true;
            }
        }
        return false;
    }

    protected long getLatestStamp() {
        return latest.getTimestamp();
    }

    public void startCoast(TelemetryPage start) {
        coastWindowOpen = true;
        coastStart = start.getTimestamp();
    }

    public void stopCoast() {
        coastWindowOpen = false;
    }
}
