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
/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cowboycoders.cyclismo.io.file;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.cowboycoders.cyclismo.R;
import org.cowboycoders.cyclismo.content.MyTracksLocation;
import org.cowboycoders.cyclismo.content.Sensor;
import org.cowboycoders.cyclismo.content.Sensor.SensorDataSet;
import org.cowboycoders.cyclismo.content.Track;
import org.cowboycoders.cyclismo.content.Waypoint;
import org.cowboycoders.cyclismo.io.file.TrackWriterFactory.TrackFileFormat;
import org.cowboycoders.cyclismo.util.StringUtils;
import org.cowboycoders.cyclismo.util.SystemUtils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Write track as TCX to a file. See http://developer.garmin.com/schemas/tcx/v2/
 * for info on TCX. <br>
 * The TCX file output is verified by uploading the file to
 * http://connect.garmin.com/.
 *
 * @author Sandor Dornbush
 * @author Dominik Rttsches
 */
public class TcxTrackWriter implements TrackFormatWriter {

  private static final String TAG = TcxTrackWriter.class.getSimpleName();

  /**
   * TCX sport type. See the TCX spec.
   *
   * @author Jimmy Shih
   */
  private enum SportType {
    RUNNING("Running"),
    BIKING("Biking"),
    OTHER("Other");

    private final String name;

    private SportType(String name) {
      this.name = name;
    }

    /**
     * Gets the name of the sport type
     */
    public String getName() {
      return name;
    }
  }

  // My Tracks categories that are considered as TCX biking sport type.
  private static final int TCX_SPORT_BIKING_IDS[] = {
      R.string.activity_type_cycling,
      R.string.activity_type_dirt_bike,
      R.string.activity_type_mountain_biking,
      R.string.activity_type_road_biking,
      R.string.activity_type_track_cycling };

  // My Tracks categories that are considered as TCX running sport type.
  private static final int TCX_SPORT_RUNNING_IDS[] = {
      R.string.activity_type_running,
      R.string.activity_type_speed_walking,
      R.string.activity_type_street_running,
      R.string.activity_type_track_running,
      R.string.activity_type_trail_running,
      R.string.activity_type_walking };

  private final Context context;
  private Track track;
  private PrintWriter printWriter;
  private SportType sportType;

  public TcxTrackWriter(Context context) {
    this.context = context;
  }

  @Override
  public void prepare(Track aTrack, OutputStream out) {
    this.track = aTrack;
    this.printWriter = new PrintWriter(out);
    this.sportType = getSportType(track.getCategory());
  }

  @Override
  public void close() {
    if (printWriter != null) {
      printWriter.close();
      printWriter = null;
    }
  }

  @Override
  public String getExtension() {
    return TrackFileFormat.TCX.getExtension();
  }

  @Override
  public void writeHeader() {
    if (printWriter != null) {
      printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      printWriter.println("<TrainingCenterDatabase"
        + " xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\""
        + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
      printWriter.println("xsi:schemaLocation=\""
        + " http://www.garmin.com/xmlschemas/ProfileExtension/v1"
        + " http://www.garmin.com/xmlschemas/UserProfilePowerExtensionv1.xsd"
        + " http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2"
        + " http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd"
        + " http://www.garmin.com/xmlschemas/UserProfile/v2"
        + " http://www.garmin.com/xmlschemas/UserProfileExtensionv2.xsd"
        + "\">");
    }
  }

  @Override
  public void writeFooter() {
    if (printWriter != null) {
      printWriter.println("<Author xsi:type=\"Application_t\">");
      printWriter.println("<Name>" 
          + StringUtils.formatCData(context.getString(R.string.send_google_by_my_tracks, "", "")) 
          + "</Name>");
      // <Build>, <LangID>, and <PartNumber> are required by type=Application_t.
      printWriter.println("<Build>");
      writeVersion();
      printWriter.println("</Build>");
      printWriter.println("<LangID>" + Locale.getDefault().getLanguage() + "</LangID>");
      printWriter.println("<PartNumber>000-00000-00</PartNumber>");
      printWriter.println("</Author>");
      printWriter.println("</TrainingCenterDatabase>");
    }
  }

  public static String getElement(String tag, float value, boolean asInt) {
    String valueStr = asInt ? Integer.toString(Math.round(value)) : Float.toString(value);
    return  "<" + tag + ">" + valueStr + "</" + tag + ">";
  }

  public static String getElement(String tag, float value) {
    return TcxTrackWriter.getElement(tag, value, false);
  }

  @Override
  public void writeBeginTrack(Location firstPoint) {
    if (printWriter != null) {
      String startTime = StringUtils.formatDateTimeIso8601(
          track.getTripStatistics().getStartTime());
      long totalTimeInSeconds = track.getTripStatistics().getTotalTime() / 1000;

      printWriter.println("<Activities>");
      printWriter.println("<Activity Sport=\"" + sportType.getName() + "\">");
      printWriter.println("<Id>" + startTime + "</Id>");
      printWriter.println("<Lap StartTime=\"" + startTime + "\">");
      printWriter.println("<TotalTimeSeconds>" + totalTimeInSeconds + "</TotalTimeSeconds>");
      printWriter.println("<DistanceMeters>" + track.getTripStatistics().getTotalDistance()
          + "</DistanceMeters>");
      // <Calories> is required, just put in 0.
      printWriter.println("<Calories>0</Calories>");
      printWriter.println("<Intensity>Active</Intensity>");
      printWriter.println("<TriggerMethod>Manual</TriggerMethod>");
    }
  }

  @Override
  public void writeEndTrack(Location lastPoint) {
    if (printWriter != null) {
      printWriter.println("</Lap>");
      printWriter.println("<Notes>" + StringUtils.formatCData(track.getDescription()) + "</Notes>");
      printWriter.println("<Creator xsi:type=\"Device_t\">");
      printWriter.println("<Name>" 
          + StringUtils.formatCData(context.getString(R.string.send_google_by_my_tracks, "", "")) 
          + "</Name>");
      // <UnitId>, <ProductID>, and <Version> are required for type=Device_t.
      printWriter.println("<UnitId>0</UnitId>");
      printWriter.println("<ProductID>0</ProductID>");
      writeVersion();
      printWriter.println("</Creator>");
      printWriter.println("</Activity>");
      printWriter.println("</Activities>");
    }
  }

  @Override
  public void writeOpenSegment() {
    if (printWriter != null) {
      printWriter.println("<Track>");
    }
  }

  @Override
  public void writeCloseSegment() {
    if (printWriter != null) {
      printWriter.println("</Track>");
    }
  }

  @Override
  public void writeLocation(Location location) {
    if (printWriter != null) {
      printWriter.println("<Trackpoint>");
      printWriter.println("<Time>" + StringUtils.formatDateTimeIso8601(location.getTime()) + "</Time>");
      printWriter.println("<Position>");
      printWriter.println("<LatitudeDegrees>" + location.getLatitude() + "</LatitudeDegrees>");
      printWriter.println("<LongitudeDegrees>" + location.getLongitude() + "</LongitudeDegrees>");
      printWriter.println("</Position>");
      printWriter.println("<AltitudeMeters>" + location.getAltitude() + "</AltitudeMeters>");

      if (location instanceof MyTracksLocation) {
        SensorDataSet sensorDataSet = ((MyTracksLocation) location).getSensorDataSet();
        if (sensorDataSet != null) {
          boolean heartRateAvailable = sensorDataSet.hasHeartRate()
              && sensorDataSet.getHeartRate().hasValue()
              && sensorDataSet.getHeartRate().getState() == Sensor.SensorState.SENDING;
          boolean cadenceAvailable = sensorDataSet.hasCadence()
            && sensorDataSet.getCadence().hasValue()
            && sensorDataSet.getCadence().getState() == Sensor.SensorState.SENDING;
          boolean powerAvailable = sensorDataSet.hasPower() 
            && sensorDataSet.getPower().hasValue()
            && sensorDataSet.getPower().getState() == Sensor.SensorState.SENDING;
          boolean distanceAvailable = sensorDataSet.hasDistance()
            && sensorDataSet.getDistance().hasValue()
            && sensorDataSet.getDistance().getState() == Sensor.SensorState.SENDING;
          //boolean speedAvailable = sensorDataSet.hasSpeed()
          //    && sensorDataSet.getSpeed().hasValue()
          //    && sensorDataSet.getSpeed().getState() == Sensor.SensorState.SENDING;

          // Prefer the speed measured on the Turbo. In the future, if we made adjustments to
          // the turbo speed to correct for errors in the simulation we should use the speed
          // from the location. This would also be required if we supported turbos with no
          // resistance control.
          // TODO: Get rid of this? Garmin uses the distance for speed calc
          //float speed = speedAvailable ? sensorDataSet.getSpeed().getValue() : location.getSpeed();
          //printWriter.println(TcxTrackWriter.getElement("Speed", speed));

          if (heartRateAvailable) {
            printWriter.println("<HeartRateBpm>");
            printWriter.println(TcxTrackWriter.getElement("Value",
                Math.round(sensorDataSet.getHeartRate().getValue()),
                true));
            printWriter.println("</HeartRateBpm>");
          }

          if (distanceAvailable) {
            Log.d(TAG, "Distance, sensor: " + sensorDataSet.getDistance().getValue());
            printWriter.println(TcxTrackWriter.getElement("DistanceMeters",
                sensorDataSet.getDistance().getValue()));
          }

          // <Cadence> needs to be put before <Extensions>.
          // According to the TCX spec, <Cadence> is only for the biking sport
          // type. For others, use <RunCadence> in <Extensions>.
          if (cadenceAvailable && sportType == SportType.BIKING) {
            // The spec requires the max value be 254.
            printWriter.println(TcxTrackWriter.getElement("Cadence",
                Math.min(254, sensorDataSet.getCadence().getValue()),
                true));
          }

          if ((cadenceAvailable && sportType != SportType.BIKING) || powerAvailable) {
            printWriter.println("<Extensions>");
            printWriter.println(
                "<TPX xmlns=\"http://www.garmin.com/xmlschemas/ActivityExtension/v2\">");

            // <RunCadence> needs to be put before <Watts>.
            if (cadenceAvailable && sportType != SportType.BIKING) {
              // The spec requires the max value to be 254.
              printWriter.println(TcxTrackWriter.getElement("RunCadence",
                  Math.min(254, sensorDataSet.getCadence().getValue()),
                  true));
            }

            if (powerAvailable) {
              printWriter.println(TcxTrackWriter.getElement("Watts",
                  sensorDataSet.getPower().getValue(),
                  true));
            }
            printWriter.println("</TPX>");
            printWriter.println("</Extensions>");
          }
        }
      }
      printWriter.println("</Trackpoint>");
    }
  }

  @Override
  public void writeBeginWaypoints() {
    // Do nothing.
  }

  @Override
  public void writeEndWaypoints() {
    // Do nothing.
  }

  @Override
  public void writeWaypoint(Waypoint waypoint) {
    // Do nothing.
  }

  /**
   * Writes the TCX Version.
   */
  private void writeVersion() {
    // Split the My Tracks version code into VersionMajor, VersionMinor, and,
    // BuildMajor to fit the integer type requirement for these fields in the
    // TCX spec.
    String[] versionComponents = SystemUtils.getMyTracksVersion(context).split("\\.");
    int versionMajor = versionComponents.length > 0 ? Integer.valueOf(versionComponents[0]) : 0;
    int versionMinor = versionComponents.length > 1 ? Integer.valueOf(versionComponents[1]) : 0;
    int buildMajor = versionComponents.length > 2 ? Integer.valueOf(versionComponents[2]) : 0;

    printWriter.println("<Version>");
    printWriter.println("<VersionMajor>" + versionMajor + "</VersionMajor>");
    printWriter.println("<VersionMinor>" + versionMinor + "</VersionMinor>");
    // According to TCX spec, these are optional. But http://connect.garmin.com
    // requires them.
    printWriter.println("<BuildMajor>" + buildMajor + "</BuildMajor>");
    printWriter.println("<BuildMinor>0</BuildMinor>");
    printWriter.println("</Version>");
  }

  /**
   * Gets the sport type from the category.
   *
   * @param category the category
   */
  private SportType getSportType(String category) {
    category = category.trim();

    // For tracks with localized category.
    for (int i : TCX_SPORT_RUNNING_IDS) {
      if (category.equalsIgnoreCase(context.getString(i))) {
        return SportType.RUNNING;
      }
    }
    for (int i : TCX_SPORT_BIKING_IDS) {
      if (category.equalsIgnoreCase(context.getString(i))) {
        return SportType.BIKING;
      }
    }

    // For tracks without localized category.
    if (category.equalsIgnoreCase(SportType.RUNNING.getName())) {
      return SportType.RUNNING;
    } else if (category.equalsIgnoreCase(SportType.BIKING.getName())) {
      return SportType.BIKING;
    } else {
      return SportType.OTHER;
    }
  }
}
