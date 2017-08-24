package org.fluxoid.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by fluxoid on 8/24/17.
 */

public class FixedSizeTest {

  @Test
  public void fifoOrder() {
    FixedSizeFifo<Integer> fifo = new FixedSizeFifo<>(3);
    fifo.add(1);
    fifo.add(2);
    fifo.add(3);
    assertEquals(1, (int) fifo.poll());
    assertEquals(2, (int) fifo.poll());
    assertEquals(3, (int) fifo.poll());

    //oversize
    fifo.addAll(Arrays.asList(1,2,3,4));
    assertEquals(2, (int) fifo.poll());
    assertEquals(3, (int) fifo.poll());
    assertEquals(4, (int) fifo.poll());
  }

  @Test
  public void lifoOrder() {
    FixedSizeLifo<Integer> lifo = new FixedSizeLifo<>(3);
    lifo.add(1);
    lifo.add(2);
    lifo.add(3);
    assertEquals(3, (int) lifo.poll());
    assertEquals(2, (int) lifo.poll());
    assertEquals(1, (int) lifo.poll());

    //oversize
    lifo.addAll(Arrays.asList(1,2,3,4));
    assertEquals(4, (int) lifo.poll());
    assertEquals(3, (int) lifo.poll());
    assertEquals(2, (int) lifo.poll());
  }





}
