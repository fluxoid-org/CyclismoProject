package org.fluxoid.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by fluxoid on 8/24/17.
 */


public class RunningAveragerTest {

  @Test
  public void basic() {
    RunningAverager avg = new RunningAverager(3);
    avg.add(1);
    assertEquals(1.0, avg.getAverage(), 0.001);
    avg.add(1);
    assertEquals(1.0, avg.getAverage(), 0.001);
    avg.add(1);
    assertEquals(1.0, avg.getAverage(), 0.001);
    avg.add(4);
    assertEquals(2.0, avg.getAverage(), 0.001);
    avg.add(4);
    assertEquals(3.0, avg.getAverage(), 0.001);
    avg.add(7);
    assertEquals(5.0, avg.getAverage(), 0.001);
  }

}
