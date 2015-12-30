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
 * Copyright 2009 Google Inc.
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

package org.cowboycoders.cyclismo.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ZoomControls;

import com.google.common.annotations.VisibleForTesting;

import org.cowboycoders.cyclismo.AltitudeProfileView;
import org.cowboycoders.cyclismo.R;
import org.cowboycoders.cyclismo.TrackDetailActivity;
import org.cowboycoders.cyclismo.content.MyTracksLocation;
import org.cowboycoders.cyclismo.content.Sensor;
import org.cowboycoders.cyclismo.content.Sensor.SensorDataSet;
import org.cowboycoders.cyclismo.content.Track;
import org.cowboycoders.cyclismo.content.TrackDataHub;
import org.cowboycoders.cyclismo.content.TrackDataListener;
import org.cowboycoders.cyclismo.content.TrackDataType;
import org.cowboycoders.cyclismo.content.Waypoint;
import org.cowboycoders.cyclismo.stats.TripStatistics;
import org.cowboycoders.cyclismo.stats.TripStatisticsUpdater;
import org.cowboycoders.cyclismo.util.LocationUtils;
import org.cowboycoders.cyclismo.util.PreferencesUtils;
import org.cowboycoders.cyclismo.util.UnitConversions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A fragment to display track chart to the user.
 * 
 * @author Sandor Dornbush
 * @author Rodrigo Damazio
 */
public class AltitudeProfileFragment extends Fragment implements TrackDataListener {

  public static final String TAG = "AltitudeProfileFragment";
  public static final String CHART_FRAGMENT_TAG = "altitudeProfileFragment";


  private final ArrayList<double[]> pendingPoints = new ArrayList<double[]>();
  private final ArrayList<double[]> pendingOverlayPoints = new ArrayList<double[]>();

  private TrackDataHub trackDataHub;

  private Track currentTrack;

  private Lock classLock = new ReentrantLock();

  private Condition startCondition = classLock.newCondition();

  // Stats gathered from the received datCa
  private TripStatisticsUpdater tripStatisticsUpdater;
  private TripStatisticsUpdater tripStatisticsUpdaterOverlay;
  private long startTime = -1L;

  private boolean metricUnits = PreferencesUtils.METRIC_UNITS_DEFAULT;
  private int minRecordingDistance = PreferencesUtils.MIN_RECORDING_DISTANCE_DEFAULT;

  // Modes of operation
  private boolean chartByDistance = true;
  private boolean[] chartShow = new boolean[] { true, true, true, true, true, true };

  long currentCourseId = -1L;

  // UI elements
  private AltitudeProfileView altitudeProfileView;
  private ZoomControls zoomControls;

  private boolean overlayCourseData = false;

  /**
   * A runnable that will enable/disable zoom controls and orange pointer as
   * appropriate and redraw.
   */
  private final Runnable updateChart = new Runnable() {
      @Override
    public void run() {
      if (!isResumed() || trackDataHub == null) {
        return;
      }

      zoomControls.setIsZoomInEnabled(altitudeProfileView.canZoomIn());
      zoomControls.setIsZoomOutEnabled(altitudeProfileView.canZoomOut());
      altitudeProfileView.invalidate();
    }
  };

  private TrackDataHub courseDataHub;


// would need to zoom in to relevant section to use
// (as is stretches scale too much and zoom limit prevents from seeing new data)
// also x-axis should be restricted to distance
private TrackDataListener courseTrackDataListener = new TrackDataListener() {

  private Track currentCourse;
  private boolean overlayAdded = false;

  @Override
  public void onLocationStateChanged(LocationState state) {
    // We don't care.
  }

  @Override
  public void onLocationChanged(Location loc) {
    // We don't care.
  }

  @Override
  public void onHeadingChanged(double heading) {
    // We don't care.
  }

  @Override
  public void onSelectedTrackChanged(Track track) {
    // We don't care.
  }

  @Override
  public void onTrackUpdated(Track track) {
    if (isResumed()) {
      Log.d(TAG,"course updated");
      currentCourse = track;
   }
  }

  @Override
  public synchronized void clearTrackPoints() {
    if (isResumed()) {
//      Log.d(TAG,"track points cleared");

      try {
      // FIXME: appears to be waiting for startTime, but could have our own startTime, as
      // in track loader
      while(startTime == -1l) {
        try {
          classLock.lock();
          startCondition.await();
        } finally {
          classLock.unlock();
        }
      }

      } catch (InterruptedException e) {
        // use start time as is
      }

      tripStatisticsUpdaterOverlay = startTime != -1L ? new TripStatisticsUpdater(startTime) : null;
//      pendingPoints.clear();
//      altitudeProfileView.reset();
//      getActivity().runOnUiThread(new Runnable() {
//          @Override
//        public void run() {
//          if (isResumed()) {
//            altitudeProfileView.resetScroll();
//          }
//        }
//      });
    }
  }

  @Override
  public synchronized void onSampledInTrackPoint(Location location) {
    if (isResumed()) {
      Log.d(TAG,"adding course point");
      double[] data = new double[AltitudeProfileView.NUM_SERIES + 1];
      fillDataPoint(location, data, tripStatisticsUpdaterOverlay);
      pendingOverlayPoints.add(data);
    }
  }

  @Override
  public void onSampledOutTrackPoint(Location location) {

  }

  @Override
  public void onSegmentSplit(Location location) {
    if (isResumed()) {
      fillDataPoint(location, null,tripStatisticsUpdaterOverlay);
    }
  }

  @Override
  public synchronized void onNewTrackPointsDone() {
    if (isResumed()) {
      Log.d(TAG,"course points done");
      if (overlayCourseData && currentCourse != null && currentCourse.getId() == currentCourseId && !overlayAdded ) {
        altitudeProfileView.addAltitudeData(pendingOverlayPoints);
        getActivity().runOnUiThread(updateChart);
        overlayAdded = true;
      }
      pendingOverlayPoints.clear();
    }
  }

  @Override
  public void clearWaypoints() {

  }

  @Override
  public void onNewWaypoint(Waypoint waypoint) {
//    if (isResumed() && waypoint != null && LocationUtils.isValidLocation(waypoint.getLocation())) {
//      altitudeProfileView.addWaypoint(waypoint);
//    }
  }

  @Override
  public void onNewWaypointsDone() {
//    if (isResumed()) {
//      getActivity().runOnUiThread(updateChart);
//    }
  }

  @Override
  public boolean onMetricUnitsChanged(boolean metric) {
    return false;
  }

  @Override
  public boolean onReportSpeedChanged(boolean speed) {
    return false;

  }

  @Override
  public boolean onMinRecordingDistanceChanged(int value) {
    return false;
  }
  };




  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    /*
     * Create a altitudeProfileView here to store data thus won't need to reload all the
     * data on every onStart or onResume.
     */
    altitudeProfileView = new AltitudeProfileView(getActivity());
  };

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.altitude_profile, container, false);
    zoomControls = (ZoomControls) view.findViewById(R.id.altitude_profile_zoom_controls);
    zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
        @Override
      public void onClick(View v) {
        zoomIn();
      }
    });
    zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
        @Override
      public void onClick(View v) {
        zoomOut();
      }
    });
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    ViewGroup layout = (ViewGroup) getActivity().findViewById(R.id.altitude_profile_layout);
    LayoutParams layoutParams = new LayoutParams(
        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    layout.addView(altitudeProfileView, layoutParams);
  }

  @Override
  public void onResume() {
    //TODO: why only in course mode? We need it to work in existing rides.
    //if (((TrackDetailActivity) getActivity()).isCourseMode()) {
      overlayCourseData = true;

    //}
    resumeTrackDataHub();
    resumeCourseDataHub();

    checkChartSettings();
    getActivity().runOnUiThread(updateChart);

    // show elevation for whole course if in course mode
    if (overlayCourseData) {
      altitudeProfileView.setOverlayChartValueSeriesEnabled(AltitudeProfileView.ELEVATION_SERIES, true);
    } else {
      altitudeProfileView.setOverlayChartValueSeriesEnabled(AltitudeProfileView.ELEVATION_SERIES, false);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    pauseTrackDataHub();
    pauseCourseDataHub();
  }

  @Override
  public void onStop() {
    super.onStop();
    ViewGroup layout = (ViewGroup) getActivity().findViewById(R.id.altitude_profile_layout);
    layout.removeView(altitudeProfileView);
  }

  @Override
  public void onLocationStateChanged(LocationState state) {
    // We don't care.
  }

  @Override
  public void onLocationChanged(Location loc) {
    // We don't care.
  }

  @Override
  public void onHeadingChanged(double heading) {
    // We don't care.
  }

  @Override
  public void onSelectedTrackChanged(Track track) {
    // We don't care.
  }

  @Override
  public void onTrackUpdated(Track track) {
    if (isResumed()) {
      Log.d(TAG,"track updated");
      currentTrack = track;
      if (track == null || track.getTripStatistics() == null) {
        startTime = -1L;
        return;
      }
      startTime = track.getTripStatistics().getStartTime();
      try {
        classLock.lock();
        startCondition.signalAll();
      } finally {
        classLock.unlock();
      }
    }
  }

  @Override
  public synchronized void clearTrackPoints() {
    if (isResumed()) {
      Log.d(TAG,"track points cleared");
      tripStatisticsUpdater = startTime != -1L ? new TripStatisticsUpdater(startTime) : null;
      pendingPoints.clear();
      altitudeProfileView.reset();
      getActivity().runOnUiThread(new Runnable() {
          @Override
        public void run() {
          if (isResumed()) {
            altitudeProfileView.resetScroll();
          }
        }
      });
    }
  }

  @Override
  public synchronized void onSampledInTrackPoint(Location location) {
    if (isResumed()) {
      Log.d(TAG,"adding point");
      Log.d(TAG, "onSampliedInTrack elevation: " + location.getAltitude());
      double[] data = new double[AltitudeProfileView.NUM_SERIES + 1];

      fillDataPoint(location, data,tripStatisticsUpdater);
      pendingPoints.add(data);
    }
  }

  @Override
  public void onSampledOutTrackPoint(Location location) {
    if (isResumed()) {
      fillDataPoint(location, null,tripStatisticsUpdater);
    }
  }

  @Override
  public void onSegmentSplit(Location location) {
    if (isResumed()) {
      fillDataPoint(location, null,tripStatisticsUpdater);
    }
  }

  @Override
  public synchronized void onNewTrackPointsDone() {
    if (isResumed()) {
      Log.d(TAG,"track points done");
      altitudeProfileView.addDataPoints(pendingPoints);
      pendingPoints.clear();
      getActivity().runOnUiThread(updateChart);
    }
  }

  @Override
  public void clearWaypoints() {
    if (isResumed()) {
      altitudeProfileView.clearWaypoints();
    }
  }

  @Override
  public void onNewWaypoint(Waypoint waypoint) {
    if (isResumed() && waypoint != null && LocationUtils.isValidLocation(waypoint.getLocation())) {
      altitudeProfileView.addWaypoint(waypoint);
    }
  }

  @Override
  public void onNewWaypointsDone() {
    if (isResumed()) {
      getActivity().runOnUiThread(updateChart);
    }
  }

  @Override
  public boolean onMetricUnitsChanged(boolean metric) {
    if (isResumed()) {
      if (metricUnits == metric) {
        return false;
      }
      metricUnits = metric;
      altitudeProfileView.setMetricUnits(metricUnits);
      getActivity().runOnUiThread(new Runnable() {
          @Override
        public void run() {
          if (isResumed()) {
            altitudeProfileView.requestLayout();
          }
        }
      });
      return true;
    }
    return false;
  }

  @Override
  public boolean onReportSpeedChanged(boolean speed) {
    return false;
  }

  @Override
  public boolean onMinRecordingDistanceChanged(int value) {
    if (isResumed()) {
      if (minRecordingDistance == value) {
        return false;
      }
      minRecordingDistance = value;
      return true;
    }
    return false;
  }

  /**
   * Checks the chart settings.
   */
  private void checkChartSettings() {
    boolean needUpdate = false;
    if (chartByDistance != PreferencesUtils.getBoolean(getActivity(),
        R.string.chart_by_distance_key, PreferencesUtils.CHART_BY_DISTANCE_DEFAULT)) {
      chartByDistance = !chartByDistance;
      altitudeProfileView.setChartByDistance(chartByDistance);
      reloadTrackDataHub();
      reloadCourseDataHub();
      needUpdate = true;
    }
    if (setSeriesEnabled(AltitudeProfileView.ELEVATION_SERIES, PreferencesUtils.getBoolean(getActivity(),
        R.string.chart_show_elevation_key, PreferencesUtils.CHART_SHOW_ELEVATION_DEFAULT))) {
      needUpdate = true;
    }

    if (needUpdate) {
      altitudeProfileView.postInvalidate();
    }
  }

  /**
   * Sets the series enabled value.
   *
   * @param index the series index
   * @param value the value
   * @return true if changed
   */
  private boolean setSeriesEnabled(int index, boolean value) {
    if (chartShow[index] != value) {
      chartShow[index] = value;
      altitudeProfileView.setChartValueSeriesEnabled(index, value);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Resumes the trackDataHub. Needs to be synchronized because trackDataHub can
   * be accessed by multiple threads.
   */
  private synchronized void resumeTrackDataHub() {
    trackDataHub = ((TrackDetailActivity) getActivity()).getTrackDataHub();
    trackDataHub.registerTrackDataListener(this, EnumSet.of(TrackDataType.TRACKS_TABLE,
        TrackDataType.WAYPOINTS_TABLE, TrackDataType.SAMPLED_IN_TRACK_POINTS_TABLE,
        TrackDataType.SAMPLED_OUT_TRACK_POINTS_TABLE, TrackDataType.PREFERENCE));
  }

  /**
   * Pauses the trackDataHub. Needs to be synchronized because trackDataHub can
   * be accessed by multiple threads.
   */
  private synchronized void pauseTrackDataHub() {
    trackDataHub.unregisterTrackDataListener(this);
    trackDataHub = null;
  }

  /**
   * Returns true if the selected track is recording. Needs to be synchronized
   * because trackDataHub can be accessed by multiple threads.
   */
  private synchronized boolean isSelectedTrackRecording() {
    return trackDataHub != null && trackDataHub.isSelectedTrackRecording();
  }

  /**
   * Reloads the trackDataHub. Needs to be synchronized because trackDataHub can
   * be accessed by multiple threads.
   */
  private synchronized void reloadTrackDataHub() {
    if (trackDataHub != null) {
      trackDataHub.reloadDataForListener(this);
    }
  }

  /**
   * Reloads the courseDataHub. Needs to be synchronized because trackDataHub can
   * be accessed by multiple threads.
   */
  private synchronized void reloadCourseDataHub() {
    if (courseDataHub != null) {
      courseDataHub.loadTrack(currentCourseId);
      courseDataHub.reloadDataForListener(courseTrackDataListener);
    }

  }

  /**
   * Pauses the trackDataHub. Needs to be synchronized because the trackDataHub
   * can be accessed by multiple threads.
   */
  private synchronized void pauseCourseDataHub() {
    if (courseDataHub != null) {
      courseDataHub.unregisterTrackDataListener(courseTrackDataListener);
    }
    courseDataHub = null;
  }

  /**
   * Resumes the trackDataHub. Needs to be synchronized because the trackDataHub
   * can be accessed by multiple threads.
   */
  private synchronized void resumeCourseDataHub() {
    if (overlayCourseData) {
      TrackDetailActivity activity = (TrackDetailActivity) getActivity();
      courseDataHub = activity.getCourseDataHub();

      if (activity.isCourseMode()) {
        currentCourseId = activity.getCourseTrackId();
      } else {
        currentCourseId = activity.getTrackId();
        courseDataHub = activity.getTrackDataHub();
      }

    courseDataHub.registerTrackDataListener(courseTrackDataListener, EnumSet.of(TrackDataType.TRACKS_TABLE,
        TrackDataType.SELECTED_TRACK,
        TrackDataType.WAYPOINTS_TABLE, TrackDataType.SAMPLED_IN_TRACK_POINTS_TABLE,
        TrackDataType.LOCATION));
    reloadCourseDataHub();

    }

  }


  /**
   * To zoom in.
   */
  private void zoomIn() {
    altitudeProfileView.zoomIn();
    zoomControls.setIsZoomInEnabled(altitudeProfileView.canZoomIn());
    zoomControls.setIsZoomOutEnabled(altitudeProfileView.canZoomOut());
  }

  /**
   * To zoom out.
   */
  private void zoomOut() {
    altitudeProfileView.zoomOut();
    zoomControls.setIsZoomInEnabled(altitudeProfileView.canZoomIn());
    zoomControls.setIsZoomOutEnabled(altitudeProfileView.canZoomOut());
  }

  /**
   * Given a location, fill in a data point, an array of double[]. <br>
   * data[0] = time/distance <br>
   * data[1] = elevation <br>
   * data[2] = speed <br>
   * data[3] = pace <br>
   * data[4] = heart rate <br>
   * data[5] = cadence <br>
   * data[6] = power <br>
   *
   * @param location the location
   * @param data the data point to fill in, can be null
   */
  @VisibleForTesting
  void fillDataPoint(Location location, double data[], TripStatisticsUpdater tripStatisticsUpdaterIn) {
    double timeOrDistance = Double.NaN;
    double elevation = Double.NaN;

    if (tripStatisticsUpdaterIn != null) {
      tripStatisticsUpdaterIn.addLocation(location, minRecordingDistance);
      TripStatistics tripStatistics = tripStatisticsUpdaterIn.getTripStatistics();
      if (chartByDistance) {
        double distance = tripStatistics.getTotalDistance() * UnitConversions.M_TO_KM;
        if (!metricUnits) {
          distance *= UnitConversions.KM_TO_MI;
        }
        timeOrDistance = distance;
      } else {
        timeOrDistance = tripStatistics.getTotalTime();
      }
      elevation = tripStatisticsUpdaterIn.getSmoothedElevation();
      if (!metricUnits) {
        elevation *= UnitConversions.M_TO_FT;
      }

    if (data != null) {
      data[0] = timeOrDistance;
      data[1] = elevation;
      Log.d(TAG, "filldataPoint: elevation: " + elevation);
    }
  }}

  /**
   * Non-overlay default {@link org.cowboycoders.cyclismo.fragments.AltitudeProfileFragment#fillDataPoint(android.location.Location, double[], org.cowboycoders.cyclismo.stats.TripStatisticsUpdater)}
   * @param location
   * @param data
   */
  @VisibleForTesting
  void fillDataPoint(Location location, double data[]) {
    this.fillDataPoint(location, data, tripStatisticsUpdater);
  }

  @VisibleForTesting
  AltitudeProfileView getAltitudeProfileView() {
    return altitudeProfileView;
  }

  @VisibleForTesting
  void setTripStatisticsUpdater(long time) {
    tripStatisticsUpdater = new TripStatisticsUpdater(time);
  }

  @VisibleForTesting
  void setAltitudeProfileView(AltitudeProfileView view) {
    altitudeProfileView = view;
  }

  @VisibleForTesting
  void setMetricUnits(boolean value) {
    metricUnits = value;
  }


  @VisibleForTesting
  void setChartByDistance(boolean value) {
    chartByDistance = value;
  }
  
}
