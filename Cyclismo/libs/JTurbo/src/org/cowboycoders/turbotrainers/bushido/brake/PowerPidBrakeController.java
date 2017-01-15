package org.cowboycoders.turbotrainers.bushido.brake;

import org.cowboycoders.pid.GainController;
import org.cowboycoders.pid.GainParameters;
import org.cowboycoders.pid.OutputControlParameters;
import org.cowboycoders.pid.OutputController;
import org.cowboycoders.pid.PidController;
import org.cowboycoders.pid.ProcessVariableProvider;
import org.cowboycoders.turbotrainers.Mode;
import org.fluxoid.utils.SimpleCsvLogger;

import java.io.File;

public class PowerPidBrakeController extends AbstractController {

  private static String TAG = PowerPidBrakeController.class.getSimpleName();

  private static final Mode SUPPORTED_MODE = Mode.TARGET_POWER;

  private static final double PID_PROPORTIONAL_GAIN = 10.0;
  private static final double PID_INTEGRAL_GAIN = 4.0;
  private static final double PID_DERIVATIVE_GAIN = 1.0;

  private static final double STARTING_RESISTANCE_PERCENT = 0.0;

  // TODO(doug): Make this configurable
  private static final double POWER_THRESHOLD_W = 50;
  private static final double TINY_POWER_W = 1;

  // Choose a low default so the resistance stays low until this is updated externally.
  private double targetPowerW = POWER_THRESHOLD_W;

  // This is accessed concurrently
  private volatile double actualPower = -1.0;

  private OutputController resistanceOutputController = new OutputController() {

    @Override
    public void setOutput(double resistance) {
      log("Setting trainer resistance in % to: " + resistance);
      BrakeModel bushidoDataModel = getDataModel();
      logToCsv(ABSOLUTE_RESISTANCE_HEADING, bushidoDataModel.getAbsoluteResistance());
      bushidoDataModel.setResistance(resistance);
    }

    @Override
    public double getMaxOutput() {
      return BrakeModel.RESISTANCE_MAX;
    }

    @Override
    public double getMinOutput() {
      return BrakeModel.RESISTANCE_MIN;
    }
  };

  private ProcessVariableProvider powerProvider = new ProcessVariableProvider() {
    @Override
    public double getProcessVariable() {
      // Using normalised power
      return (targetPowerW < TINY_POWER_W) ? 0.0 : actualPower / targetPowerW;
    }
  };

  private GainController resistanceGainController = new GainController() {
    @Override
    public GainParameters getGain(OutputControlParameters parameters) {
      GainParameters defaultParameters = new GainParameters(
          PID_PROPORTIONAL_GAIN, PID_INTEGRAL_GAIN, PID_DERIVATIVE_GAIN);
      return defaultParameters;
    }
  };

  private PidController pidController = new PidController(
      powerProvider, resistanceOutputController, resistanceGainController);

  protected void setActualPower(double power) {
    logToCsv(POWER_HEADING, power);
    log("setActualPower: " + power);
    actualPower = power;
  }

  public void setTargetPower(double power) {
    log("setTargetPower: " + power);
    targetPowerW = power;
  }

  @Override
  public double onPowerChange(double power) {
    setActualPower(power);
    if (power <= POWER_THRESHOLD_W) {
      log("Power is less than the threshold, resetting PID controller.");
      System.out.println();
      pidController.reset();
    }
    log("Triggering update of PID controller.");
    // Using normalised power, so this is always 1.
    pidController.adjustSetpoint(1.0);
    return power;
  }

  @Override
  public Mode getMode() {
    return SUPPORTED_MODE;
  }

  @Override
  public void onStart() {
    BrakeModel bushidoDataModel = getDataModel();
    bushidoDataModel.setResistance(STARTING_RESISTANCE_PERCENT);
  }

  protected void log(String msg) {
    System.out.println(TAG + ": " + msg);
  }

  @Override
  protected SimpleCsvLogger getCsvLogger(File file) {
    SimpleCsvLogger logger = new SimpleCsvLogger(file, POWER_HEADING, ABSOLUTE_RESISTANCE_HEADING);
    logger.addTime(true);
    logger.append(true);
    return logger;
  }

  @Override
  public void onStop() {
  }

}
