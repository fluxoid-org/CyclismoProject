/**
 * Copyright (c) 2013, Will Szumski
 *
 * This file is part of formicidae.
 *
 * formicidae is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * formicidae is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with formicidae.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fluxoid.utils;

import java.util.Iterator;

/*
 * TODO: remove this class
 *
 * This is a peculiarity as (currently) the oldest element is removed
 * when the fifo reaches its max size. This seems incongruous with its
 * stated fifo functionality. It acts more like a rolling window, that
 * returns the first elements in the window.
 */

/**
 * @param <V> type of elements in the fifo
 */
public class FixedSizeFifo<V> extends AbstractFixedSizeQueue<V> {

  public FixedSizeFifo(int maxSize) {
    super(maxSize);
  }

  @Override
  public V peek() {
    return queue.peek();
  }

  @Override
  public V poll() {
    return queue.poll();
  }

  @Override
  public Iterator<V> iterator() {
    return queue.iterator();
  }

}
