/*
*    Copyright (c) 2013, Will Szumski
*    Copyright (c) 2013, Doug Szumski
*
*    This file is part of Cyclismo.
*
*    Cyclismo is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Cyclismo is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Cyclismo.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.fluxoid.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Smooths course point altitudes caused by limited GPS altitude resolution.
 */
public class AltitudeSmoother {

  // Used for interpolation of track points
  private static final double DEFAULT_MINIMUM_TRACK_POINT_SPACING_M = 10.0;
  // Used for smoothing the altitude data
  private static final int DEFAULT_AVERAGING_SWEEPS = 2;
  // Size of the averaging window in points
  private static final int DEFAULT_AVERAGING_WINDOW_SIZE_POINTS = 100;

  private double minTrackPointSpacing;
  private int averagingSweeps;
  private int averagingWindowSize;

  public AltitudeSmoother() {
    this.averagingSweeps = DEFAULT_AVERAGING_SWEEPS;
    this.averagingWindowSize = DEFAULT_AVERAGING_WINDOW_SIZE_POINTS;
    this.minTrackPointSpacing = DEFAULT_MINIMUM_TRACK_POINT_SPACING_M;
  }

  public AltitudeSmoother setMinTrackPointSpacing(double minTrackPointSpacing) {
    this.minTrackPointSpacing = minTrackPointSpacing;
    return this;
  }

  public AltitudeSmoother setAveragingSweeps(int averagingSweeps) {
    this.averagingSweeps = averagingSweeps;
    return this;
  }

  public AltitudeSmoother setAveragingWindowSize(int averagingWindowSize) {
    this.averagingWindowSize = averagingWindowSize;
    return this;
  }

  /**
   * Smooth the track point altitudes.
   * <ul>
   * <li>Interpolate points to uniform spacing<li>
   * <li>Run a moving average over the interpolated points</li>
   * </ul>
   * This is necessary because even a +/- 1m error in GPS altitude can cause large
   * apparent gradients. To avoid the rider hitting a 'brick wall' some filtering /smoothing /
   * interpolation of the gradient is required.
   *
   * TODO: Use a median filter to remove anomalies?
   * TODO: Convert to centred window
   * TODO: Use a more elaborate scheme: Interpolation of gradients? Kalman? Kernel smoothing?
   *
   * @param trackPoints Points to smooth.
   */
  public List<LatLongAlt> smoothTrackPointAltitudes(List<LatLongAlt> trackPoints) {
    // Interpolate and smooth the data points to prevent large changes in the simulated gradient.
    trackPoints = LocationUtils.interpolatePoints(trackPoints, this.minTrackPointSpacing);
    List<LatLongAlt> newPoints = new ArrayList<LatLongAlt>();
    // Smooth the altitudes
    for (int i = 0; i < this.averagingSweeps; ++i) {
      RunningAverager runningAverager = new RunningAverager(this.averagingWindowSize);
      for (LatLongAlt p : trackPoints) {
        runningAverager.add(p.getAltitude());
        LatLongAlt newLoc = new LatLongAlt(p.getLatitude(), p.getLongitude(), runningAverager
            .getAverage());
        newPoints.add(newLoc);
      }
    }
    return newPoints;
  }

}
