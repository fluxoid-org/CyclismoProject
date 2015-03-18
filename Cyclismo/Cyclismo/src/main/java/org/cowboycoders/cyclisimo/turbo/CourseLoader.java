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
package org.cowboycoders.cyclisimo.turbo;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.cowboycoders.cyclisimo.content.Track;
import org.cowboycoders.cyclisimo.content.TrackDataHub;
import org.cowboycoders.cyclisimo.content.TrackDataListener;
import org.cowboycoders.cyclisimo.content.TrackDataType;
import org.cowboycoders.cyclisimo.content.Waypoint;
import org.cowboycoders.location.LatLongAlt;
import org.cowboycoders.location.LocationUtils;
import org.cowboycoders.utils.RunningAverager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CourseLoader {

  public static final double MINIMUM_TRACK_POINT_SPACING_M = 10.0;

  public static String TAG = CourseLoader.class.getSimpleName();
  
  private TrackDataHub courseDataHub;
  private List<LatLongAlt> latLongAlts = new ArrayList<LatLongAlt>();
  private boolean finished = false;
  private Track currentTrack;
  private final long expectedId;
  private LatLongAlt lastAddedPoint = null;

  CourseLoader(Context context, long trackId) {
    courseDataHub = TrackDataHub.newInstance(context,true);
    expectedId = trackId;
    courseDataHub.start();
    courseDataHub.registerTrackDataListener(trackDataListener, EnumSet.of(TrackDataType.TRACKS_TABLE,
        TrackDataType.SAMPLED_IN_TRACK_POINTS_TABLE));
    loadCourse(trackId);
  }
  
  private void loadCourse(long trackId) {
    //courseDataHub.loadTrack(-1);
    courseDataHub.loadTrack(trackId);
    courseDataHub.reloadDataForListener(trackDataListener);
  }
  
  private TrackDataListener trackDataListener = new TrackDataListener() {
    @Override
    public void onLocationStateChanged(LocationState locationState) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onLocationChanged(Location location) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onHeadingChanged(double heading) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onSelectedTrackChanged(Track track) {
      Log.d(TAG,"selected track changed");
      
    }

    @Override
    public void onTrackUpdated(Track track) {
      Log.d(TAG,"track updated");
      currentTrack = track;
      
    }

    @Override
    public void clearTrackPoints() {
      // TODO Auto-generated method stub
      
    }

    @Override
    public synchronized void onSampledInTrackPoint(Location location) {
      if (finished)
          return;

      // TODO: Check location is in decimal degrees
      LatLongAlt point = new LatLongAlt(
              location.getLatitude(),
              location.getLongitude(),
              location.getAltitude());
      Log.d(TAG, point.toString());

      if (lastAddedPoint == null) {
          lastAddedPoint = point;
          Log.i(TAG, "first track point");
          latLongAlts.add(point);
          return;
      }

      double pointSep = LocationUtils.getDistance(lastAddedPoint, point);

      if (pointSep >  MINIMUM_TRACK_POINT_SPACING_M) {
          Log.i(TAG,"added track point, spacing " + LocationUtils.getDistance(lastAddedPoint, point));
          lastAddedPoint = point;
          latLongAlts.add(point);
      } else {
          Log.i(TAG,"filtered track point");
      }

    }

    @Override
    public void onSampledOutTrackPoint(Location location) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onSegmentSplit(Location location) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public synchronized void onNewTrackPointsDone() {
      Log.i(TAG, "track points done");
      if (currentTrack != null && currentTrack.getId() == expectedId ) {
        finished = true;
        // TODO: Move to own method
        // Interpolate and smooth the data points to prevent large changes in the simulated gradient.
        latLongAlts = LocationUtils.interpolatePoints(latLongAlts, MINIMUM_TRACK_POINT_SPACING_M);
        // Smooth the altitudes

        for (int i = 0; i < 3; ++i) {
            RunningAverager runningAverager = new RunningAverager(100);
            for (LatLongAlt p: latLongAlts) {
                runningAverager.add(p.getAltitude());
                p.setAltitude(runningAverager.getAverage());
            }
        }

        gradientsToLog();

        courseDataHub.stop();
        courseDataHub.unregisterTrackDataListener(this);
        this.notifyAll();
      } else if (!finished) {
        latLongAlts.clear();
      }
    }

    @Override
    public void clearWaypoints() {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onNewWaypoint(Waypoint waypoint) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onNewWaypointsDone() {
      // TODO Auto-generated method stub
      
    }

    @Override
    public boolean onMetricUnitsChanged(boolean metricUnits) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean onReportSpeedChanged(boolean reportSpeed) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean onMinRecordingDistanceChanged(int minRecordingDistance) {
      // TODO Auto-generated method stub
      return false;
    }
  };
  
  public List<LatLongAlt> getLatLongAlts() throws InterruptedException {

    synchronized (trackDataListener) {
      while (!finished) {
        trackDataListener.wait();
      }
    }

    Log.i(TAG,"lat long length: " + latLongAlts.size());

    return latLongAlts;
  }

  /**
  * Log gradients for course points array.
  */
  public void gradientsToLog() {
    Log.d(TAG, "gradient log start");
    for (int i = 1; i < latLongAlts.size(); ++i){
      Log.i(TAG,
              "gradient: " +
                LocationUtils.getLocalisedGradient(latLongAlts.get(i - 1), latLongAlts.get(i))
                    + " point: "
                        + latLongAlts.get(i).toString()
      );
    }
    Log.i(TAG, "gradient log stop");
  }

}
