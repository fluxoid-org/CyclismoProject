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
 * Copyright 2012 Google Inc.
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

package org.cowboycoders.cyclismo.util;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.cowboycoders.cyclismo.R;
import org.cowboycoders.cyclismo.content.MyTracksLocation;
import org.cowboycoders.cyclismo.content.Sensor;
import org.cowboycoders.cyclismo.stats.TripStatistics;

/**
 * Utilities for updating the statistics UI labels and values.
 *
 * @author Jimmy Shih
 */
public class StatsUtils {

  public static final String TAG = "StatsUtils";

  private StatsUtils() {}

  /**
   *  Set sensor data values
   *
   * @param activity the activity
   * @param location the location containing the sensor data
   */
  public static void setSensorDataValues(Activity activity, MyTracksLocation location) {
    // Try and get sensor data
    Sensor.SensorDataSet sensorDataSet;
    if (location.getSensorDataSet() != null) {
      sensorDataSet = location.getSensorDataSet();
    } else {
      Log.d(TAG, "Failed to get sensor data");
      return;
    }

    // Set instantaneous power
    boolean showPower = PreferencesUtils.getBoolean(
            activity, R.string.stats_show_power_key, PreferencesUtils.STATS_SHOW_POWER_DEFAULT);
    View powerLabelTableRow = activity.findViewById(R.id.stats_power_label_table_row);
    View powerValueTableRow = activity.findViewById(R.id.stats_power_value_table_row);
    if (powerLabelTableRow == null || powerValueTableRow == null) {
      return;
    }
    powerLabelTableRow.setVisibility(showPower ? View.VISIBLE : View.GONE);
    powerValueTableRow.setVisibility(showPower ? View.VISIBLE : View.GONE);

    double instPower = Double.NaN;
    if (showPower && sensorDataSet!= null) {
      if (sensorDataSet.hasPower()
              && sensorDataSet.getPower().getState() == Sensor.SensorState.SENDING
              && sensorDataSet.getPower().hasValue()) {
        instPower = sensorDataSet.getPower().getValue();
      }
    }
    setSensorValue(activity, R.id.stats_inst_power_value, instPower, R.string.value_integer_power);

    // Set instantaneous cadence
    boolean showCadence = PreferencesUtils.getBoolean(
            activity, R.string.stats_show_cadence_key, PreferencesUtils.STATS_SHOW_CADENCE_DEFAULT);
    View cadenceLabelTableRow = activity.findViewById(R.id.stats_cadence_label_table_row);
    View cadenceValueTableRow = activity.findViewById(R.id.stats_cadence_value_table_row);
    if (cadenceLabelTableRow == null || cadenceValueTableRow == null) {
      return;
    }
    cadenceLabelTableRow.setVisibility(showCadence ? View.VISIBLE : View.GONE);
    cadenceValueTableRow.setVisibility(showCadence ? View.VISIBLE : View.GONE);

    double instCadence = Double.NaN;
    if (showCadence && sensorDataSet!= null) {
      if (sensorDataSet.hasCadence()
              && sensorDataSet.getCadence().getState() == Sensor.SensorState.SENDING
              && sensorDataSet.getCadence().hasValue()) {
        instCadence = sensorDataSet.getCadence().getValue();
      }
    }
    setSensorValue(activity, R.id.stats_inst_cadence_value, instCadence, R.string.value_integer_cadence);

    // Set instantaneous heart rate
    boolean showHeartRate = PreferencesUtils.getBoolean(
            activity, R.string.stats_show_heartrate_key, PreferencesUtils.STATS_SHOW_HEARTRATE_DEFAULT);
    View heartRateLabelTableRow = activity.findViewById(R.id.stats_heartrate_label_table_row);
    View heartRateValueTableRow = activity.findViewById(R.id.stats_heartrate_value_table_row);
    if (heartRateLabelTableRow == null || heartRateValueTableRow == null) {
      return;
    }
    heartRateLabelTableRow.setVisibility(showHeartRate ? View.VISIBLE : View.GONE);
    heartRateValueTableRow.setVisibility(showHeartRate ? View.VISIBLE : View.GONE);

    double instHeartRate = Double.NaN;
    if (showHeartRate && sensorDataSet!= null) {
      if (sensorDataSet.hasHeartRate()
              && sensorDataSet.getHeartRate().getState() == Sensor.SensorState.SENDING
              && sensorDataSet.getHeartRate().hasValue()) {
        instHeartRate = sensorDataSet.getHeartRate().getValue();
      }
    }
    setSensorValue(activity, R.id.stats_inst_heartrate_value, instHeartRate, R.string.value_integer_heartrate);
  }

  /**
   * Sets the location values.
   *
   * @param activity the activity
   * @param location the location
   * @param showAll true to show all the fields, false to show only the
   *          elevation field
   */
  public static void setLocationValues(Activity activity, Location location, boolean showAll) {
    boolean metricUnits = PreferencesUtils.getBoolean(
        activity, R.string.metric_units_key, PreferencesUtils.METRIC_UNITS_DEFAULT);
    boolean reportSpeed = PreferencesUtils.getBoolean(
        activity, R.string.report_speed_key, PreferencesUtils.REPORT_SPEED_DEFAULT);

    // Set elevation
    boolean showElevation = PreferencesUtils.getBoolean(
        activity, R.string.stats_show_elevation_key, PreferencesUtils.STATS_SHOW_ELEVATION_DEFAULT);
    if (showElevation) {
      double altitude = location == null ? Double.NaN : location.getAltitude();
      setElevationValue(activity, R.id.stats_elevation_value, altitude, metricUnits);
    }

    if (!showAll) {
      return;
    }

    // Set speed/pace
    setSpeedLabel(
        activity, R.id.stats_speed_label, R.string.stats_speed, R.string.stats_pace, reportSpeed);
    double speed = location == null ? Double.NaN : location.getSpeed();
    setSpeedValue(activity, R.id.stats_speed_value, speed, metricUnits, reportSpeed);

    // Set coordinate
    boolean showCoordinate = PreferencesUtils.getBoolean(activity,
        R.string.stats_show_coordinate_key, PreferencesUtils.STATS_SHOW_COORDINATE_DEFAULT);
    View coordinateLabelTableRow = activity.findViewById(R.id.stats_coordinate_label_table_row);
    View coordinateValueTableRow = activity.findViewById(R.id.stats_coordinate_value_table_row);
    if (coordinateLabelTableRow == null || coordinateValueTableRow == null) {
      return;
    }
    coordinateLabelTableRow.setVisibility(showCoordinate ? View.VISIBLE : View.GONE);
    coordinateValueTableRow.setVisibility(showCoordinate ? View.VISIBLE : View.GONE);
    if (showCoordinate) {
      double latitude = location == null ? Double.NaN : location.getLatitude();
      double longitude = location == null ? Double.NaN : location.getLongitude();
      setCoordinateValue(activity, R.id.stats_latitude_value, latitude);
      setCoordinateValue(activity, R.id.stats_longitude_value, longitude);
    }
  }

  /**
   * Sets the total time value.
   *
   * @param activity the activity
   * @param totalTime the total time
   */
  public static void setTotalTimeValue(Activity activity, long totalTime) {
    setTimeValue(activity, R.id.stats_total_time_value, totalTime);
  }

  /**
   * Sets the trip statistics values.
   *
   * @param activity the activity
   * @param tripStatistics the trip statistics
   */
  public static void setTripStatisticsValues(Activity activity, TripStatistics tripStatistics) {
    boolean metricUnits = PreferencesUtils.getBoolean(
        activity, R.string.metric_units_key, PreferencesUtils.METRIC_UNITS_DEFAULT);
    boolean reportSpeed = PreferencesUtils.getBoolean(
        activity, R.string.report_speed_key, PreferencesUtils.REPORT_SPEED_DEFAULT);
    boolean showTotalTime = PreferencesUtils.getBoolean(activity,
        R.string.stats_show_total_time_key, PreferencesUtils.STATS_SHOW_TOTAL_TIME_DEFAULT);
    boolean showMovingTime = PreferencesUtils.getBoolean(activity,
        R.string.stats_show_moving_time_key, PreferencesUtils.STATS_SHOW_MOVING_TIME_DEFAULT);
    
    // Set total distance
    double totalDistance = tripStatistics == null ? Double.NaN : tripStatistics.getTotalDistance();
    setDistanceValue(activity, R.id.stats_distance_value, totalDistance, metricUnits);
    
    // Set total time
    setItemVisibility(activity, R.id.stats_total_time_label, -1,
        R.id.stats_total_time_value, showTotalTime);
    if (showTotalTime) {
      setTimeValue(activity, R.id.stats_total_time_value,
          tripStatistics != null ? tripStatistics.getTotalTime() : -1L);
    }

    // Set moving time. Only show the spacer if total time is shown.
    setItemVisibility(activity, R.id.stats_moving_time_label, R.id.stats_moving_time_spacer,
        R.id.stats_moving_time_value, showMovingTime, showTotalTime);
    if (showMovingTime) {
      setTimeValue(activity, R.id.stats_moving_time_value,
          tripStatistics != null ? tripStatistics.getMovingTime() : -1L);
    }

    // Set average speed
    setSpeedLabel(activity, R.id.stats_average_speed_label, R.string.stats_average_speed,
        R.string.stats_average_pace, reportSpeed);
    double averageSpeed = tripStatistics != null ? tripStatistics.getAverageSpeed() : Double.NaN;
    setSpeedValue(activity, R.id.stats_average_speed_value, averageSpeed, metricUnits, reportSpeed);
    
    // Set average moving speed
    setItemVisibility(activity, R.id.stats_average_moving_speed_label,
        R.id.stats_average_moving_speed_spacer, R.id.stats_average_moving_speed_value,
        showMovingTime);
    if (showMovingTime) {
      setSpeedLabel(activity, R.id.stats_average_moving_speed_label,
          R.string.stats_average_moving_speed, R.string.stats_average_moving_pace, reportSpeed);
      double averageMovingSpeed = tripStatistics != null ? tripStatistics.getAverageMovingSpeed()
          : Double.NaN;
      setSpeedValue(activity, R.id.stats_average_moving_speed_value, averageMovingSpeed,
          metricUnits, reportSpeed);
    }

    // TODO: Hide these fields when off in stats menu
    double avgPower = tripStatistics == null ? Double.NaN : tripStatistics.getAverageMovingPower();
    setSensorValue(activity, R.id.stats_avg_power_value, avgPower, R.string.value_integer_power);
    double avgCadence = tripStatistics == null ? Double.NaN : tripStatistics.getAverageMovingCadence();
    setSensorValue(activity, R.id.stats_avg_cadence_value, avgCadence, R.string.value_integer_cadence);
    double avgHeartRate = tripStatistics == null ? Double.NaN : tripStatistics.getAverageMovingHeartRate();
    setSensorValue(activity, R.id.stats_avg_heartrate_value, avgHeartRate, R.string.value_integer_heartrate);

    // Set max speed
    setSpeedLabel(activity, R.id.stats_max_speed_label, R.string.stats_max_speed,
        R.string.stats_fastest_pace, reportSpeed);
    double maxSpeed = tripStatistics == null ? Double.NaN : tripStatistics.getMaxSpeed();
    setSpeedValue(activity, R.id.stats_max_speed_value, maxSpeed, metricUnits, reportSpeed);
    
    // Set elevation
    boolean showElevation = PreferencesUtils.getBoolean(
        activity, R.string.stats_show_elevation_key, PreferencesUtils.STATS_SHOW_ELEVATION_DEFAULT);
    View elevationLabelTableRow1 = activity.findViewById(R.id.stats_elevation_label_table_row1);
    View elevationValueTableRow1 = activity.findViewById(R.id.stats_elevation_value_table_row1);
    View elevationLabelTableRow2 = activity.findViewById(R.id.stats_elevation_label_table_row2);
    View elevationValueTableRow2 = activity.findViewById(R.id.stats_elevation_value_table_row2);
    if (elevationLabelTableRow1 == null 
        || elevationValueTableRow1 == null
        || elevationLabelTableRow2 == null 
        || elevationValueTableRow2 == null) {
      return;
    }
    elevationLabelTableRow1.setVisibility(showElevation ? View.VISIBLE : View.GONE);
    elevationValueTableRow1.setVisibility(showElevation ? View.VISIBLE : View.GONE);
    elevationLabelTableRow2.setVisibility(showElevation ? View.VISIBLE : View.GONE);
    elevationValueTableRow2.setVisibility(showElevation ? View.VISIBLE : View.GONE);
    if (showElevation) {
      double elevationGain = tripStatistics == null ? Double.NaN
          : tripStatistics.getTotalElevationGain();
      double minElevation = tripStatistics == null ? Double.NaN : tripStatistics.getMinElevation();
      double maxElevation = tripStatistics == null ? Double.NaN : tripStatistics.getMaxElevation();
      setElevationValue(activity, R.id.stats_elevation_gain_value, elevationGain, metricUnits);
      setElevationValue(activity, R.id.stats_min_elevation_value, minElevation, metricUnits);
      setElevationValue(activity, R.id.stats_max_elevation_value, maxElevation, metricUnits);
    }

    // Set grade
    boolean showGrade = PreferencesUtils.getBoolean(
        activity, R.string.stats_show_grade_key, PreferencesUtils.STATS_SHOW_GRADE_DEFAULT);
    View gradeLabelTableRow = activity.findViewById(R.id.stats_grade_label_table_row);
    View gradeValueTableRow = activity.findViewById(R.id.stats_grade_value_table_row);
    if (gradeLabelTableRow == null || gradeValueTableRow == null) {
      return;
    }
    gradeLabelTableRow.setVisibility(showGrade ? View.VISIBLE : View.GONE);
    gradeValueTableRow.setVisibility(showGrade ? View.VISIBLE : View.GONE);
    if (showGrade) {
      double minGrade = tripStatistics == null ? Double.NaN : tripStatistics.getMinGrade();
      double maxGrade = tripStatistics == null ? Double.NaN : tripStatistics.getMaxGrade();
      setGradeValue(activity, R.id.stats_min_grade_value, minGrade);
      setGradeValue(activity, R.id.stats_max_grade_value, maxGrade);
    }
  }

  private static void setItemVisibility(
      Activity activity, int labelId, int spacerId, int valueId, boolean show) {
    setItemVisibility(activity, labelId, spacerId, valueId, show, true);
  }

  private static void setItemVisibility(
      Activity activity, int labelId, int spacerId, int valueId,
      boolean show, boolean show_spacer) {
    View label = activity.findViewById(labelId);
    View spacer = activity.findViewById(spacerId);
    View value = activity.findViewById(valueId);
    if (label != null) {
      label.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    if (spacer != null) {
      spacer.setVisibility(show_spacer ? View.VISIBLE : View.GONE);
    }
    if (value != null) {
      value.setVisibility(show ? View.VISIBLE : View.GONE);
    }
  }

  /**
   * Sets a speed label.
   *
   * @param activity the activity
   * @param id the speed label resource id
   * @param speedId the speed string id
   * @param paceId the pace string id
   * @param reportSpeed true to report speed
   */
  private static void setSpeedLabel(
      Activity activity, int id, int speedId, int paceId, boolean reportSpeed) {
    TextView textView = (TextView) activity.findViewById(id);
    if (textView == null) {
      return;
    }
    textView.setText(reportSpeed ? speedId : paceId);
  }

  /**
   * Sets a speed value.
   *
   * @param activity the activity
   * @param id the speed value resource id
   * @param speed the speed in meters per second
   * @param metricUnits true to display in metric units
   * @param reportSpeed true to report speed
   */
  private static void setSpeedValue(
      Activity activity, int id, double speed, boolean metricUnits, boolean reportSpeed) {
    TextView textView = (TextView) activity.findViewById(id);
    if (textView == null) {
      return;
    }
    textView.setText(StringUtils.formatSpeed(activity, speed, metricUnits, reportSpeed));
  }

  /**
   * Sets a distance value.
   *
   * @param activity the activity
   * @param id the distance value resource id
   * @param distance the distance in meters
   * @param metricUnits true to display in metric units
   */
  private static void setDistanceValue(
      Activity activity, int id, double distance, boolean metricUnits) {
    TextView textView = (TextView) activity.findViewById(id);
    if (textView == null) {
      return;
    }
    textView.setText(StringUtils.formatDistance(activity, distance, metricUnits));
  }

  /**
   * Sets a time value.
   *
   * @param activity the activity
   * @param id the time value resource id
   * @param time the time
   */
  private static void setTimeValue(Activity activity, int id, long time) {
    TextView textView = (TextView) activity.findViewById(id);
    if (textView == null) {
      return;
    }
    String value = time == -1L ? activity.getString(R.string.value_unknown)
        : StringUtils.formatElapsedTime(time);
    textView.setText(value);
  }

  /**
   * Sets an elevation value.
   *
   * @param activity the activity
   * @param id the elevation value resource id
   * @param elevation the elevation in meters
   * @param metricUnits true to display in metric units
   */
  private static void setElevationValue(
      Activity activity, int id, double elevation, boolean metricUnits) {
    TextView textView = (TextView) activity.findViewById(id);
    if (textView == null) {
      return;
    }
    String value;
    if (Double.isNaN(elevation) || Double.isInfinite(elevation)) {
      value = activity.getString(R.string.value_unknown);
    } else {
      if (metricUnits) {
        value = activity.getString(R.string.value_float_meter, elevation);
      } else {
        elevation *= UnitConversions.M_TO_FT;
        value = activity.getString(R.string.value_float_feet, elevation);
      }
    }
    textView.setText(value);
  }

  /**
   * Sets a grade value.
   *
   * @param activity the activity
   * @param id the grade value resource id
   * @param grade the grade in fraction between 0 and 1
   */
  private static void setGradeValue(Activity activity, int id, double grade) {
    TextView textView = (TextView) activity.findViewById(id);
    if (textView == null) {
      return;
    }
    String value;
    if (Double.isNaN(grade) || Double.isInfinite(grade)) {
      value = activity.getString(R.string.value_unknown);
    } else {
      value = activity.getString(R.string.value_integer_percent, Math.round(grade * 100));
    }
    textView.setText(value);
  }

  /**
   * Sets a sensor value.
   *
   * @param activity the activity
   * @param id the value resource id
   * @param sensorValue the sensor value
   */
  private static void setSensorValue(Activity activity, int id, double sensorValue, int format) {
    TextView textView = (TextView) activity.findViewById(id);
    if (textView == null) {
      return;
    }
    String value;
    if (Double.isNaN(sensorValue) || Double.isInfinite(sensorValue)) {
      value = activity.getString(R.string.value_unknown);
    } else {
      value = activity.getString(format, Math.round(sensorValue));
    }
    textView.setText(value);
  }

  /**
   * Sets a coordinate value.
   *
   * @param activity the activity
   * @param id the coordinate value resource id
   * @param coordinate the coordinate in degrees
   */
  private static void setCoordinateValue(Activity activity, int id, double coordinate) {
    TextView textView = (TextView) activity.findViewById(id);
    if (textView == null) {
      return;
    }
    String value;
    if (Double.isNaN(coordinate) || Double.isInfinite(coordinate)) {
      value = activity.getString(R.string.value_unknown);
    } else {
      value = activity.getString(
          R.string.value_coordinate_degree, Location.convert(coordinate, Location.FORMAT_DEGREES));
    }
    textView.setText(value);
  }
}
