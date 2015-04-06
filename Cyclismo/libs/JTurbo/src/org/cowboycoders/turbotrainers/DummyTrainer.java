package org.cowboycoders.turbotrainers;

import org.fluxoid.utils.Conversions;
import org.fluxoid.utils.TrapezoidIntegrator;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

/**
 * Created by fluxoid on 17/03/15.
 */
public class DummyTrainer extends GenericTurboTrainer {

    private static final double MAX_CADENCE_CHANGE = 2;
    private static final double MAX_POWER_CHANGE = 5;
    private static final double HR_STEP = 2; // heart rate step size
    private TrapezoidIntegrator distanceIntegrator = new TrapezoidIntegrator();

    private volatile double hr = 60;
    private volatile double power = 250;
    private volatile double cadence = 90;
    private volatile double speed = 0;
    private volatile double distance = 0;
    private volatile double slope = 0.0;

    public static final Mode [] SUPPORTED_MODES = new Mode [] {
            Mode.TARGET_SLOPE
    };

    {
        setSupportedModes(SUPPORTED_MODES);
    }

  /**
   * Crude HR model derived from curve fit
   * @param power riders current power output
   * @param heartRate previous heart rate measurement
   */
    private static int getModelHR(double power, double heartRate) {
      double targetHR;
      if (power <= 100) {
        targetHR = 60 + 0.3 * power;
      } else {
        targetHR = 56.07 * Math.log(power) - 162.43;
      }
      if (targetHR < 60) {
        targetHR = 60;
      }
      double diff = Math.abs(targetHR - heartRate);
      if (heartRate < targetHR) {
        heartRate += Math.min(diff, HR_STEP);
      } else if (heartRate > targetHR) {
        heartRate -= Math.min(diff, HR_STEP);
      }
      return (int) heartRate;
    }

    private PowerModel pm = new PowerModel();

    private static double plusOrMinus(double curVal, double maxChange) {
      double result;
      if (Math.random() < 0.5) {
        result =  curVal + Math.random() * maxChange;
      } else {
        result =  curVal - Math.random() * maxChange;
      }
      // negative values make no sense for us
      if (result < 0.0) return 0.0;
      return result;
    }

  private final class PowerModelUpdater extends TimerTask {
    @Override
    public void run() {
        power = plusOrMinus(power, MAX_POWER_CHANGE);
        pm.updatePower(power);
        speed = pm.getVelocity() * Conversions.METRES_PER_SECOND_TO_KM_PER_HOUR;
        distanceIntegrator.add(getTimestamp(), pm.getVelocity());
        distance = distanceIntegrator.getIntegral();

    }
  }

    private final class DataSpoofer extends TimerTask {
        @Override
        public void run() {
          //change these at lower frequency
          hr = getModelHR(power, hr);
          cadence = plusOrMinus(cadence, MAX_CADENCE_CHANGE);
            for (TurboTrainerDataListener listener: getDataChangeListeners()) {
                listener.onCadenceChange(cadence);
                listener.onPowerChange(power);
                listener.onHeartRateChange(hr);
                listener.onSpeedChange(speed);
                listener.onDistanceChange(distance);
            }

        }
    }



  protected static double getTimestamp() {
    return System.nanoTime() / Math.pow(10, 9);
  }

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
      if (!(parameters instanceof Parameters.TargetSlope)) {
        throw new IllegalArgumentException("this class only supports target-slope");
      }
      Parameters.TargetSlope tp = (Parameters.TargetSlope) parameters;
      slope = tp.getSlope();
      pm.setAirDensity(parameters.getAirDensity());
      pm.setCoefficentRollingResistance(parameters.getCoefficentRollingResistance());
      pm.setCurrentBearing(parameters.getCurrentBearing());
      pm.setDragArea(parameters.getDragArea());
      pm.setTotalMass(parameters.getTotalWeight());
      pm.setGradientAsPercentage(slope);
      pm.setMomentOfInertiaWheels(parameters.getMomentOfInertiaWheels());
      pm.setOutsideRadiusTire(parameters.getOutsideRadiusTire());
      pm.setWindDirectionDegrees(parameters.getWindDirectionDegrees());
      pm.setWindSpeed(parameters.getWindSpeed());
      pm.setIncrementalDragAreaSpokes(parameters.getIncrementalDragAreaSpokes());
    }

    @Override
    public double getTarget() {
        return slope;
    }

    @Override
    public void start() throws TurboCommunicationException, InterruptedException, TimeoutException, IllegalStateException {
        dataTimer =  new Timer(true);
        // update power model at high frequency
        dataTimer.scheduleAtFixedRate(new PowerModelUpdater(), 5000, 50);
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
