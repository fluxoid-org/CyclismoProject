package org.cowboycoders.ant.sensors;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Aggregates sensor data based on precedence
 * Created by fluxoid on 22/12/15.
 */
public class DataSourceCombiner {

  private final HeartRateListener listener;
  private final int timeout;
  private Timer timer;
  /**
   * between 0-100 inclusive
   */
  private volatile int precedence = 100;

  /**
   * @param merged updated with latest value from source with lowest precedence
   * @param timeoutMillis timeout until precedence resets
   */
  public DataSourceCombiner(HeartRateListener merged, int timeoutMillis) {
    this.listener = merged;
    this.timeout = timeoutMillis;
    this.timer = new Timer(true);
  }


  /**
   * Poll with latest values
   *
   * @param value latest sensor data
   * @param precedence 0-100 inclusive; lower numbers have higher precedence (i.e are more
   * believable)
   */
  public void update(int value, int precedence) {
    if (precedence < 0 || precedence > 100) {
      throw new IllegalArgumentException("precedence out of range");
    }

    if (this.precedence < precedence) return;
    timer.cancel();
    this.timer = new Timer(true);
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            DataSourceCombiner.this.precedence = 100;
          }
        }, timeout);
    this.precedence = precedence;
    listener.onValueChange(value);
  }
}
