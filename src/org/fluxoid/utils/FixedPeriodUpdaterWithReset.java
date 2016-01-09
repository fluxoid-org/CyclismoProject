package org.fluxoid.utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fluxoid on 02/01/16.
 */
public abstract class FixedPeriodUpdaterWithReset extends FixedPeriodUpdater {

  private final long timeout;
  private Timer t;

   /*
   *
   * @param initialValue will be rest with this value after timeout has elapsed
   * @param callback polled at regular intervals
   * @param period polling period
   */
  public FixedPeriodUpdaterWithReset(Object initialValue, UpdateCallback callback, long period, long timeout) {
    super(initialValue, callback, period);
    t = new Timer(true);
    this.timeout = timeout;
  }

  @Override
  public synchronized void update(Object o) {
    super.update(o);
    t.cancel();
    t = new Timer(true);
    TimerTask task = new TimerTask() {

      @Override
      public void run() {
        FixedPeriodUpdaterWithReset.this.update(getResetValue());
      }
    };
    t.schedule(task, timeout);

  }

  public abstract Object getResetValue();

}
