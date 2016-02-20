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

public class LatLong {

  private double latitude;
  private double longitude;

  /**
   * @param latitude - Latitude (decimal degrees)
   * @param longitude - Longitude (decimal degrees)
   */
  public LatLong(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  /**
   * Converts lat/long in micro decimal degrees to decimal degrees.
   *
   * @param uLatitude Latitude in micro decimal degrees
   * @param uLongitude Longitude in micro decimal degrees
   * @return Lat/long in decimal degrees
   */
  public static LatLong fromMicroDegrees(int uLatitude, int uLongitude) {
    double latitude = (uLatitude / Math.pow(10, 6));
    double longitude = (uLongitude / Math.pow(10, 6));
    return new LatLong(latitude, longitude);
  }

  public static int toMicroDegrees(double number) {
    return (int) Math.round(number * Math.pow(10, 6));
  }

}
