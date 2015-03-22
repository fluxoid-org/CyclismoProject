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
package org.cowboycoders.cyclisimo;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.cowboycoders.cyclisimo.maps.AugmentedPolyline;
import org.mapsforge.map.android.view.MapView;

import org.cowboycoders.cyclisimo.MapOverlay.CachedLocation;
import org.cowboycoders.cyclisimo.content.Waypoint;
import org.cowboycoders.cyclisimo.maps.CourseTrackPath;
import org.cowboycoders.cyclisimo.maps.TrackPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An overlay that doesn't clear map built from a list of locations
 * @author will
 *
 */
public class StaticOverlay implements MarkerSource {
  
  private static final float WAYPOINT_X_ANCHOR = 13f / 48f;

  private static final float WAYPOINT_Y_ANCHOR = 43f / 48f;
  private static final float MARKER_X_ANCHOR = 50f / 96f;
  private static final float MARKER_Y_ANCHOR = 90f / 96f;
  private static final int INITIAL_LOCATIONS_SIZE = 1024;
  private final MapMarkerUpdater mapMarkerUpdater = new MapMarkerUpdater(this);

  private TrackPath trackPath;
  
  private ArrayList<AugmentedPolyline> paths = new ArrayList<AugmentedPolyline>();
  
  private List<CachedLocation> locations;
  
  private boolean showEndMarker = true;
  
  private List<Waypoint> waypoints = new ArrayList<Waypoint>();

  private Context context;

    @Override
    public float getWaypointXAnchor() {
        return WAYPOINT_X_ANCHOR;
    }

    @Override
    public float getWaypointYAnchor() {
        return WAYPOINT_Y_ANCHOR;
    }

    @Override
    public float getMarkerXAnchor() {
        return MARKER_X_ANCHOR;
    }

    @Override
    public float getMarkerYAnchor() {
        return MARKER_Y_ANCHOR;
    }

    @Override
    public Drawable getWaypoint(Waypoint waypoint) {
        int drawableId = waypoint.getType() == Waypoint.TYPE_STATISTICS ? R.drawable.yellow_pushpin
                : R.drawable.blue_pushpin;
        return context.getResources().getDrawable(drawableId);
    }

    @Override
    public Drawable getStopMarker() {
        return getContext().getResources().getDrawable(R.drawable.red_dot);
    }

    @Override
    public Drawable getStartMarker() {
        return getContext().getResources().getDrawable(R.drawable.green_dot);
    }


    /**
   * @return the waypoints
   */
  @Override
  public List<Waypoint> getWaypoints() {
    return waypoints;
  }



  /**
   * @param waypoints the waypoints to set
   */
  public void setWaypoints(List<Waypoint> waypoints) {
    this.waypoints = waypoints;
  }



  /**
   * @return the trackPath
   */
  public TrackPath getTrackPath() {
    return trackPath;
  }



  /**
   * @param trackPath the trackPath to set
   */
  public void setTrackPath(TrackPath trackPath) {
    this.trackPath = trackPath;
  }



  /**
   * @return the paths
   */
  public ArrayList<AugmentedPolyline> getPaths() {
    return paths;
  }



  /**
   * @param paths the paths to set
   */
  public void setPaths(ArrayList<AugmentedPolyline> paths) {
    this.paths = paths;
  }



  /**
   * @return the locations
   */
  @Override
  public List<CachedLocation> getLocations() {
    return locations;
  }



  /**
   * @param locations the locations to set
   */
  public void setLocations(List<CachedLocation> locations) {
    this.locations = locations;
  }



  /**
   * @return the showEndMarker
   */
  @Override
  public boolean isShowEndMarker() {
    return showEndMarker;
  }



  /**
   * @param showEndMarker the showEndMarker to set
   */
  public void setShowEndMarker(boolean showEndMarker) {
    this.showEndMarker = showEndMarker;
  }



  public StaticOverlay(Context context, List<CachedLocation> locations) {
    this.trackPath = new CourseTrackPath(context);
    this.context = context;
    // make a copy, so some fool can't add more locations (that would be me)
    setLocations(Arrays.asList(locations.toArray(new CachedLocation[0])));
  }
  
  
  
  /**
   * Updates the track, start and end markers, and waypoints.
   * 
   * @param googleMap the osm map
   */
  public void update(MapView googleMap) {

    synchronized (locations) {
      // Merge pendingLocations with locations
      @SuppressWarnings("hiding")
      TrackPath trackPath = getTrackPath();
      paths.clear();
      trackPath.updatePath(googleMap, paths, 0, locations);
        mapMarkerUpdater.updateStartAndEndMarkers(googleMap);
        mapMarkerUpdater.updateWaypoints(googleMap);

    }
  }
  
  /**
   * Updates the start and end markers.
   * 
   * @param googleMap the google map
   */
  protected void updateStartAndEndMarkers(MapView googleMap) {
    // Add the end marker

      // Add the start marker
      mapMarkerUpdater.updateStartAndEndMarkers(googleMap);
  }

  /**
   * Updates the waypoints.
   * 
   * @param googleMap the google map.
   */
  protected void updateWaypoints(MapView googleMap) {
      mapMarkerUpdater.updateWaypoints(googleMap);
  }

    protected Context getContext() {
        return context;
    }

    public void destroy() {
        this.mapMarkerUpdater.destroy();
    }
}


