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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fluxoid.utils.Conversions;
import org.fluxoid.utils.LatLongAlt;
import org.fluxoid.utils.LocationUtils;
import org.fluxoid.utils.TrapezoidIntegrator;

public class CourseTracker {

  private final double resolution;
  
  private Map<Double,LatLongAlt> distanceLocationMap = new HashMap<Double,LatLongAlt>();
  
  private Double[] distanceMarkers;
  
  private int lastKnownDistanceMarkerIndex = 0;

  private TrapezoidIntegrator distanceIntegrator = new TrapezoidIntegrator();

  /**
   * Maps absolute distance travelled to course points.
   *
   * @param coursePoints to map distance from.
   * @param resolution for interpolation
   */
  public CourseTracker(List<LatLongAlt> coursePoints, double resolution) {
    this.resolution = resolution;
    double totalDistance = 0.0;
    distanceLocationMap.put(totalDistance,coursePoints.get(0));
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
  }

  /**
   * Must be polled at frequency >= 1Hz
   */
  public void updateSpeed(double speed) {
    double timeStampSeconds = System.nanoTime() / (Math.pow(10, 9));
    double speedMetresPerSecond = speed * Conversions.KM_PER_HOUR_TO_METRES_PER_SECOND;
    distanceIntegrator.add(timeStampSeconds, speedMetresPerSecond);
  }

  public double getDistance() {
    return distanceIntegrator.getIntegral();
  }

  /**
   * Returns the nearest location to the specified distance.
   *
   * FIXME: Two distance, locations: (25.0, locA), (30.0, locB), we are at 26.0
   *
   * In this case distance - key is not less than min point spacing, so we go to the next location,
   * which is not the closest.
   *
   * Although this never returns the same location?
   *
   * @return the difference.
   */
  public LatLongAlt getNearestLocation() {
    double distance = distanceIntegrator.getIntegral();
    Double key = null;
    Double nextKey = null;
    double delta = 0.0;
    int lastI = 0;
    for (int i = lastKnownDistanceMarkerIndex; i < distanceMarkers.length; i++) {
      lastI = i;
      key = distanceMarkers[i];
      lastKnownDistanceMarkerIndex = i;
      delta = distance - key;
      if (delta < resolution) {
        break;
      }
    }

    if (lastI < distanceMarkers.length - 1) {
      nextKey = distanceMarkers[lastI + 1];
    } else { // must have reached end
      key = distanceMarkers[distanceMarkers.length -1];
      nextKey = distanceMarkers[distanceMarkers.length -1];
    }

    LatLongAlt previous = distanceLocationMap.get(key);
    LatLongAlt next = distanceLocationMap.get(nextKey);
    List<LatLongAlt> interpolated = LocationUtils.interpolateBetweenPoints(previous, next, resolution);
    Iterator<LatLongAlt> iter = interpolated.iterator();
    int requiredIterations = (int) Math.round (delta / resolution);

    LatLongAlt current = previous;
    for (int i = 0; i < requiredIterations; i++) {
      if (iter.hasNext()) {
        current = iter.next();
      } else {
        current = next;
      }
    }

    return current;
}
  
  public boolean hasFinished() {
    if (lastKnownDistanceMarkerIndex < distanceMarkers.length -1){
      return false;
    }
    return true;
  }
  
  public double getCurrentGradient() {
    if (hasFinished()) {
      return 0.0;
    }
    final Double currentLocationKey = distanceMarkers[lastKnownDistanceMarkerIndex];
    final Double nextLocationKey = distanceMarkers[lastKnownDistanceMarkerIndex +1];
    double gradient = LocationUtils
        .getLocalisedGradient(distanceLocationMap.get(currentLocationKey), distanceLocationMap.get(nextLocationKey));
    return gradient;
  }
  
  public static void main(String [] args) {
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