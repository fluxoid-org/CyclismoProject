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
import java.util.List;
import java.util.Map;

import org.fluxoid.utils.LatLongAlt;
import org.fluxoid.utils.LocationUtils;

public class CourseTracker {
  
  private static final double MIN_POINT_SPACING_M = 0.001;
  
  private Map<Double,LatLongAlt> distanceLocationMap = new HashMap<Double,LatLongAlt>();
  
  private Double[] distanceMarkers;
  
  private int lastKnownDistanceMarkerIndex = 0;

  /**
   * Maps absolute distance travelled to course points.
   *
   * @param coursePoints to map distance from.
   */
  public CourseTracker(List<LatLongAlt> coursePoints) {
    double totalDistance = 0.0;
    distanceLocationMap.put(totalDistance,coursePoints.get(0));
    for (int i = 1; i < coursePoints.size(); i++) {
      double distanceBetweenPoints = LocationUtils.getGradientCorrectedDistance(
              coursePoints.get(i - 1), coursePoints.get(i));
      totalDistance += distanceBetweenPoints;
      if (distanceBetweenPoints < MIN_POINT_SPACING_M) {
        // assume the same location
        continue;
      }
      distanceLocationMap.put(totalDistance, coursePoints.get(i));
    }
    distanceMarkers = distanceLocationMap.keySet().toArray(new Double[0]);
    Arrays.sort(distanceMarkers);
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
   * @param distance is the distance travelled.
   * @param location TODO
   * @return the difference.
   */
  public double getNearestLocation(final double distance, LatLongAlt location) {
    Double key = null;
    double delta = 0.0;
    for (int i = lastKnownDistanceMarkerIndex; i < distanceMarkers.length; i++) {
      key = distanceMarkers[i];
      lastKnownDistanceMarkerIndex = i;
      delta = distance - key;
      if (delta < MIN_POINT_SPACING_M) {
        break;
      }
    }
    if (key == null) { // must have reached end
      key = distanceMarkers[distanceMarkers.length -1];
    }

    // FIXME: Some neater way..
    LatLongAlt closest = distanceLocationMap.get(key);
    location.setLatitude(closest.getLatitude());
    location.setLongitude(closest.getLongitude());
    location.setAltitude(closest.getAltitude());

    return delta;
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
    CourseTracker ct = new CourseTracker(locations);
    
    for (Double marker : ct.distanceMarkers) {
      System.out.println(marker);
    }
    double distance = 0;
    while (!ct.hasFinished()) {
      double delta = ct.getNearestLocation(distance += 1000, new LatLongAlt(0,0,0));
      System.out.println("lastKnownDistanceMarkerIndex " + ct.lastKnownDistanceMarkerIndex
              + ", delta: " + delta);
    }
    System.out.println("finished");
  }
  
}