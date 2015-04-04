package org.cowboycoders.turbotrainers;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

/**
 * Created by fluxoid on 17/03/15.
 */
public class DummyTrainer extends GenericTurboTrainer {

    public static final Mode [] SUPPORTED_MODES = new Mode [] {
            Mode.TARGET_SLOPE
    };

    {
        setSupportedModes(SUPPORTED_MODES);
    }

    private double distance = 0.0;

    private final class DataSpoofer extends TimerTask {
        @Override
        public void run() {
            for (TurboTrainerDataListener listener: getDataChangeListeners()) {
                listener.onCadenceChange(75.0*Math.random());
                listener.onPowerChange(500.0*Math.random());
                listener.onHeartRateChange(200.0*Math.random());
                listener.onSpeedChange(5.00*Math.random());
                distance += 5 * Math.random();
                listener.onDistanceChange(distance);
            }

        }
    };

    private Timer dataTimer =  null;


    @Override
    public boolean supportsSpeed() {
        return true;
    }

    @Override
    public boolean supportsPower() {
        return true;
    }

    @Override
    public boolean supportsCadence() {
        return true;
    }

    @Override
    public boolean supportsHeartRate() {
        return true;
    }

    @Override
    public void setParameters(Parameters.CommonParametersInterface parameters) throws IllegalArgumentException {

    }

    @Override
    public double getTarget() {
        return 0;
    }

    @Override
    public void start() throws TurboCommunicationException, InterruptedException, TimeoutException, IllegalStateException {
        dataTimer =  new Timer(true);
        // spoof some data every second
        dataTimer.scheduleAtFixedRate(new DataSpoofer(), 5000, 1000);
    }

    @Override
    public void stop() throws InterruptedException, TimeoutException {
        if (dataTimer != null) {
            dataTimer.cancel();
            dataTimer = null;
        }
    }
}
