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
package org.cowboycoders.turbotrainers;

import org.fluxoid.utils.Conversions;
import org.fluxoid.utils.LatLongAlt;
import org.fluxoid.utils.LocationUtils;
import org.fluxoid.utils.TrapezoidIntegrator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseTracker {
  
  public static final double ZERO_CUTOFF = 0.0001;
  private final double resolution;

  private Map<Double, LatLongAlt> distanceLocationMap = new HashMap<Double, LatLongAlt>();

  private Double[] distanceMarkers;

  private int lastKnownDistanceMarkerIndex = 0;

  private double speed = 0.0;
  private Double lastTimeStamp;
  private Double currentTimeStamp;
  private TrapezoidIntegrator distanceIntegrator = new TrapezoidIntegrator();
  private LatLongAlt nearestLocation;

  /**
   * Maps absolute distance travelled to course points.
   *
   * @param coursePoints to map distance from.
   * @param resolution   for interpolation
   */
  public CourseTracker(List<LatLongAlt> coursePoints, double resolution) {
    this.resolution = resolution;
    double totalDistance = 0.0;
    distanceLocationMap.put(totalDistance, coursePoints.get(0));
    for (int i = 1; i < coursePoints.size(); i++) {
      double distanceBetweenPoints = LocationUtils.getGradientCorrectedDistance(
              coursePoints.get(i - 1), coursePoints.get(i));
      totalDistance += distanceBetweenPoints;
      if (distanceBetweenPoints < resolution) {
        // assume the same location
        continue;
      }
      distanceLocationMap.put(totalDistance, coursePoints.get(i));
    }
    distanceMarkers = distanceLocationMap.keySet().toArray(new Double[0]);
    Arrays.sort(distanceMarkers);
    nearestLocation = distanceLocationMap.get(distanceMarkers[0]);
  }

  /**
   * Must be polled at frequency >= 1Hz
   */
  public void updateSpeed(double newSpeed) {
    // we assume speed doesn't change much between updates
    this.speed = newSpeed * Conversions.KM_PER_HOUR_TO_METRES_PER_SECOND;
    lastTimeStamp = currentTimeStamp;
    currentTimeStamp = System.nanoTime() / (Math.pow(10, 6));

    distanceIntegrator.add(System.nanoTime() / (Math.pow(10, 9)), speed);
    double distance = distanceIntegrator.getIntegral();
    if (newSpeed < ZERO_CUTOFF) {
      // assume haven't moved
      return;
    }

    Double key;
    Double nextKey;
    double delta;

    // loop gives us index of last marker passed
    for (int i = lastKnownDistanceMarkerIndex; i < distanceMarkers.length; i++) {
      key = distanceMarkers[i];
      lastKnownDistanceMarkerIndex = i;
      delta = distance - key;
      if (delta < resolution) {
        if (delta < 0) {
          // we haven't yet reached the point, so pick the one previous
          lastKnownDistanceMarkerIndex = i - 1;
          if (lastKnownDistanceMarkerIndex < 0) {
            //special case for the first point
            lastKnownDistanceMarkerIndex = 0;
          }
          //key = distanceMarkers[lastKnownDistanceMarkerIndex];
        }
        break;
      }
    }

    if (lastKnownDistanceMarkerIndex < distanceMarkers.length - 1) {
      nextKey = distanceMarkers[lastKnownDistanceMarkerIndex + 1];
    } else { // must have reached end
      nearestLocation = distanceLocationMap.get(distanceMarkers[distanceMarkers.length - 1]);
      return;
    }
    LatLongAlt previous = getNearestLocation();
    LatLongAlt next = distanceLocationMap.get(nextKey);
    if (lastTimeStamp == null) {
      //only polled speed once
      return;
    }
    double timeDelta = currentTimeStamp - lastTimeStamp;
    LatLongAlt current;
    while ((current = LocationUtils.getLocationBetweenPoints(previous, next, speed, timeDelta)) == null) {
      lastKnownDistanceMarkerIndex++;
      if (lastKnownDistanceMarkerIndex < distanceMarkers.length - 1) {
        nextKey = distanceMarkers[lastKnownDistanceMarkerIndex + 1];
      } else { // must have reached end
        current = distanceLocationMap.get(nextKey);
        break;
      }
      next = distanceLocationMap.get(nextKey);
    }
    //distance += LocationUtils.getDistance(previous, current);
    nearestLocation = current;
  }

  public double getDistance() {
    return distanceIntegrator.getIntegral();
  }

  /**
   * @returns the nearest location on the course (that has been passed)
   */
  public LatLongAlt getNearestLocation() {
    return nearestLocation;
  }

  public boolean hasFinished() {
    if (lastKnownDistanceMarkerIndex < distanceMarkers.length - 1) {
      return false;
    }
    return true;
  }

  public double getCurrentGradient() {
    if (hasFinished()) {
      return 0.0;
    }
    final Double nextLocationKey = distanceMarkers[lastKnownDistanceMarkerIndex + 1];
    final Double previousLocationKey = distanceMarkers[lastKnownDistanceMarkerIndex];
    final LatLongAlt next = distanceLocationMap.get(nextLocationKey);
    final LatLongAlt previous = distanceLocationMap.get(previousLocationKey);
    // as we are interpolating elevation data between markers, we don't gain any precision
    // by using the currentLocation (getCurrentLocation) compared to using the previous marker
    double gradient = LocationUtils
            .getLocalisedGradient(previous, next);
    if (gradient == Double.NaN) {
      //two points at same lat/long
      return 0.0;
    }
    return gradient;
  }
  
  public static void main(String[] args) {
    LatLongAlt l1 = new LatLongAlt(50.066389, 5.715, 1000);
    LatLongAlt l2 = new LatLongAlt(58.643889, 3.07, 4000);
    List<LatLongAlt> locations = LocationUtils.interpolateBetweenPoints(l1, l2, 1000);
    locations.addAll(LocationUtils.interpolateBetweenPoints(l2, l1, 1000)); // Back to start
    CourseTracker ct = new CourseTracker(locations, 0.001);

    for (Double marker : ct.distanceMarkers) {
      System.out.println(marker);
    }

    System.out.println("finished");
  }

}