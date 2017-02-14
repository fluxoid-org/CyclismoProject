package org.cowboycoders.turbotrainers;

import org.cowboycoders.turbotrainers.Parameters.CommonParametersInterface;
import org.cowboycoders.turbotrainers.bushido.headunit.AbstractBushidoModel;
import org.fluxoid.utils.Constants;
import org.fluxoid.utils.TrapezoidIntegrator;

import java.util.logging.Logger;

abstract public class TurboBaseModel {

  public static Logger LOGGER = Logger.getLogger(AbstractBushidoModel.class.getSimpleName());
  public static byte WEIGHT_DEFAULT = 70;
  public static byte WEIGHT_LOW_LIMIT = 40;
  private double actualDistance;
  private double totalWeight = WEIGHT_DEFAULT;
  private double virtualSpeed;
  private double actualSpeed;
  private double cadence;
  private double heartRate;
  private double power;
  private double targetPowerW;
  private double slope;

  //Integrators for calculating distance travelled from virtual and actual speeds
  TrapezoidIntegrator integralVirtualSpeed = new TrapezoidIntegrator();
  TrapezoidIntegrator integralActualSpeed = new TrapezoidIntegrator();

  /**
   * @return slope as percentage
   */
  public double getSlope() {
    return slope;
  }

  /**
   * @param slope as percentage
   */
  public void setSlope(double slope) {
    this.slope = slope;
  }

  /**
   * @return the heartRate in bpm
   */
  public double getHeartRate() {
    return heartRate;
  }

  /**
   * @param heartRate in bpm
   */
  public void setHeartRate(double heartRate) {
    this.heartRate = heartRate;
  }

  /**
   * @return virtual speed in kmph
   */
  public double getVirtualSpeed() {
    return virtualSpeed;
  }

  /**
   * Speed with gradient compensation
   *
   * @param virtualSpeed in kmph
   */
  public void setVirtualSpeed(double virtualSpeed) {
    this.virtualSpeed = virtualSpeed;
    //Update the distance from the new speed reading
    double timeStampSeconds = System.nanoTime() / (Math.pow(10, 9));
    double speedMetresPerSecond = 1000 * virtualSpeed / (60 * 60);
    integralVirtualSpeed.add(timeStampSeconds, speedMetresPerSecond);
  }

  /**
   * @return actual speed in kmph
   */
  public double getActualSpeed() {
    return actualSpeed;
  }

  public void setActualSpeed(double actualSpeed) {
    this.actualSpeed = actualSpeed;
    //Update the distance from the new speed reading
    double timeStampSeconds = System.nanoTime() / (Math.pow(10, 9));
    double speedMetresPerSecond = 1000 * actualSpeed / (60 * 60);
    integralActualSpeed.add(timeStampSeconds, speedMetresPerSecond);
  }

  /**
   * @return the cadence as rpm
   */
  public double getCadence() {
    return cadence;
  }

  /**
   * @param cadence in rpm
   */
  public void setCadence(double cadence) {
    this.cadence = cadence;
  }

  /**
   * @return the power as Watts
   */
  public double getPower() {
    return power;
  }

  /**
   * @param power in Watts
   */
  public void setPower(double power) {
    this.power = power;
  }

  public void setTargetPower(double powerW) {
    this.targetPowerW = powerW;
  }

  public double getTargetPower() {
    return targetPowerW;
  }

  /**
   * True distance (travelled by wheel) as opposed to integrated speed
   *
   * @param actualDistance actualDistance in m
   */
  public void setActualDistance(double actualDistance) {
    this.actualDistance = actualDistance;
  }

  /**
   * @return actual distance in m
   */
  public double getActualDistance() {
    return actualDistance;
  }

  /**
   * @param useIntegral result obtained through integrating actual speed
   * @return actual distance in m
   */
  public double getActualDistance(boolean useIntegral) {
    if (useIntegral == false) return getActualDistance();
    return integralActualSpeed.getIntegral();
  }

  /**
   * @return virtual distance in m
   */
  public double getVirtualDistance() {
    return integralVirtualSpeed.getIntegral();
  }

  /**
   * Bike + rider weight
   *
   * @return weight in kilos
   */
  public double getTotalWeight() {
    return totalWeight;
  }

  /**
   * Bike + rider weight
   *
   * @param totalWeight in kilos
   */
  public void setTotalWeight(double totalWeight) {
    if (totalWeight > Constants.UNSIGNED_BYTE_MAX_VALUE) {
      totalWeight = Constants.UNSIGNED_BYTE_MAX_VALUE;
      LOGGER.warning("Rider is too heavy, using :" + Constants.UNSIGNED_BYTE_MAX_VALUE);
    } else if (totalWeight < 0) {
      totalWeight = WEIGHT_DEFAULT;
      LOGGER.warning("Negative rider weight, using: " + WEIGHT_DEFAULT);
    } else if (totalWeight < WEIGHT_LOW_LIMIT) {
      LOGGER.warning("Rider weight low, is this correct? Weight : " + totalWeight);
    }
    this.totalWeight = totalWeight;
  }

  /**
   * @throws IllegalArgumentException if cannot be cast to desired type
   */
  public abstract void setParameters(CommonParametersInterface parameters) throws
      IllegalArgumentException;

}
