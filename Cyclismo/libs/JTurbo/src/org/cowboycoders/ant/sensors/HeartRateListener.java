package org.cowboycoders.ant.sensors;

/**
 * Created by fluxoid on 22/12/15.
 */
public interface HeartRateListener {
  /**
   * @param hr new heart rate value
   */
  public void onValueChange(int hr);

}
