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
 * Copyright 2008 Google Inc.
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

package org.cowboycoders.cyclisimo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import org.cowboycoders.cyclisimo.maps.AugmentedPolyline;
import org.mapsforge.core.graphics.Bitmap;

import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;

import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;

import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.android.view.MapView;

import org.cowboycoders.cyclisimo.content.Waypoint;
import org.cowboycoders.cyclisimo.maps.TrackPath;
import org.cowboycoders.cyclisimo.maps.TrackPathFactory;
import org.cowboycoders.cyclisimo.util.LocationUtils;
import org.cowboycoders.cyclisimo.util.PreferencesUtils;
import org.cowboycoders.cyclisimo.util.UnitConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A map overlay that displays my location arrow, error circle, and track info.
 * 
 * @author Leif Hendrik Wilden
 */
public class MapOverlay {
  
  public static final String TAG = MapOverlay.class.getSimpleName();

  public static final float WAYPOINT_X_ANCHOR = 13f / 48f;

  private static final float WAYPOINT_Y_ANCHOR = 43f / 48f;
  private static final float MARKER_X_ANCHOR = 50f / 96f;
  private static final float MARKER_Y_ANCHOR = 90f / 96f;
  private static final int INITIAL_LOCATIONS_SIZE = 1024;

  private final OnSharedPreferenceChangeListener
      sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
          @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
          if (key == null
              || key.equals(PreferencesUtils.getKey(context, R.string.track_color_mode_key))) {
            trackColorMode = PreferencesUtils.getString(
                context, R.string.track_color_mode_key, PreferencesUtils.TRACK_COLOR_MODE_DEFAULT);
            trackPath = TrackPathFactory.getTrackPath(context, trackColorMode);
          }
        }
      };

  private final Context context;
  private final List<CachedLocation> locations;
  private final BlockingQueue<CachedLocation> pendingLocations;
  private final List<Waypoint> waypoints;
  private Layer endMarker;
  private Layer startMarker;
  
  

  /**
   * @return the pendingLocations
   */
  protected BlockingQueue<CachedLocation> getPendingLocations() {
    return pendingLocations;
  }

  /**
   * @return the locations
   */
  public List<CachedLocation> getLocations() {
    return locations;
  }
  

  /**
   * @return the waypoints
   */
  public List<Waypoint> getWaypoints() {
    return waypoints;
  }



  private String trackColorMode = PreferencesUtils.TRACK_COLOR_MODE_DEFAULT;

  private boolean showEndMarker = true;
  private TrackPath trackPath;

  private StaticOverlay underlay;

  
  
  /**
   * @return the context
   */
  public Context getContext() {
    return context;
  }

  /**
   * @return the trackPath
   */
  public TrackPath getTrackPath() {
    return trackPath;
  }

  /**
   * A pre-processed {@link Location} to speed up drawing.
   * 
   * @author Jimmy Shih
   */
  public static class CachedLocation {

    private final boolean valid;
    private final LatLong latLng;
    private final int speed;

    /**
     * Constructor for an invalid cached location.
     */
    public CachedLocation() {
      this.valid = false;
      this.latLng = null;
      this.speed = -1;
    }

    /**
     * Constructor for a potentially valid cached location.
     */
    public CachedLocation(Location location) {
      this.valid = LocationUtils.isValidLocation(location);
      this.latLng = valid ? new LatLong(location.getLatitude(), location.getLongitude()) : null;
      this.speed = (int) Math.floor(location.getSpeed() * UnitConversions.MS_TO_KMH);
    }

    /**
     * Returns true if the location is valid.
     */
    public boolean isValid() {
      return valid;
    }

    /**
     * Gets the speed in kilometers per hour.
     */
    public int getSpeed() {
      return speed;
    }

    /**
     * Gets the LatLong.
     */
    public LatLong getLatLong() {
      return latLng;
    }
  };

  public MapOverlay(Context context) {
    this.context = context;
    this.waypoints = new ArrayList<Waypoint>();
    this.locations = new ArrayList<CachedLocation>(INITIAL_LOCATIONS_SIZE);
    this.pendingLocations = new ArrayBlockingQueue<CachedLocation>(
        Constants.MAX_DISPLAYED_TRACK_POINTS, true);

    context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE)
        .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    sharedPreferenceChangeListener.onSharedPreferenceChanged(null, null);
  }

  /**
   * Add a location.
   * 
   * @param location the location
   */
  public void addLocation(Location location) {
    // Queue up in the pendingLocations until it's merged with locations
    if (!pendingLocations.offer(new CachedLocation(location))) {
      Log.e(TAG, "Unable to add to pendingLocations.");
    }
  }

  /**
   * Adds a segment split.
   */
  public void addSegmentSplit() {
    // Queue up in the pendingLocations until it's merged with locations
    if (!pendingLocations.offer(new CachedLocation())) {
      Log.e(TAG, "Unable to add to pendingLocations.");
    }
  }

  /**
   * Clears the locations.
   */
  public void clearPoints() {
    synchronized (locations) {
      locations.clear();
      pendingLocations.clear();
    }
  }

  /**
   * Adds a waypoint.
   * 
   * @param waypoint the waypoint
   */
  public void addWaypoint(Waypoint waypoint) {
    synchronized (waypoints) {
      waypoints.add(waypoint);
    }
  }

  /**
   * Clears the waypoints.
   */
  public void clearWaypoints() {
    synchronized (waypoints) {
      waypoints.clear();
    }
  }

  /**
   * Sets whether to show the end marker.
   * 
   * @param show true to show the end marker
   */
  public void setShowEndMarker(boolean show) {
    showEndMarker = show;
  }

  /**
   * Updates the track, start and end markers, and waypoints.
   * 
   * @param googleMap the google map
   * @param paths the paths
   * @param reload true to reload all points
   */
  public void update(MapView googleMap, ArrayList<AugmentedPolyline> paths, boolean reload) {
    synchronized (locations) {
      // Merge pendingLocations with locations
      @SuppressWarnings("hiding")
      TrackPath trackPath = getTrackPath();
      int newLocations = pendingLocations.drainTo(locations);
      boolean needReload = reload || trackPath.updateState();
      if (needReload) {
        Layers layers = googleMap.getLayerManager().getLayers();
        Layer map = layers.get(0);
        // clear all layers apart from the map layer
        layers.clear();
        layers.add(map);
        paths.clear();
        if (underlay != null) {
          updateUnderlay(googleMap);
        }
        trackPath.updatePath(googleMap, paths, 0, locations);
        updateStartAndEndMarkers(googleMap);
        updateWaypoints(googleMap);
      } else {
        if (newLocations != 0) {
          int numLocations = locations.size();
          trackPath.updatePath(googleMap, paths, numLocations - newLocations, locations);
        }
      }
        if (startMarker != null && endMarker != null) {
            double minLat;
            double maxLat;
            double minLong;
            double maxLong;

            if (startMarker.getPosition().latitude < endMarker.getPosition().latitude) {
                minLat = startMarker.getPosition().latitude;
                maxLat = endMarker.getPosition().latitude;
            } else {
                minLat = endMarker.getPosition().latitude;
                maxLat = startMarker.getPosition().latitude;
            }


            if (startMarker.getPosition().longitude < endMarker.getPosition().longitude) {
                minLong = startMarker.getPosition().longitude;
                maxLong = endMarker.getPosition().longitude;
            } else {
                minLong = endMarker.getPosition().longitude;
                maxLong = startMarker.getPosition().longitude;
            }

            BoundingBox bb = new BoundingBox(minLat,minLong, maxLat, maxLong);
            Dimension dimension = googleMap.getModel().mapViewDimension.getDimension();
            googleMap.getModel().mapViewPosition.setMapPosition(new MapPosition(
                    bb.getCenterPoint(),
                    LatLongUtils.zoomForBounds(
                            dimension,
                            bb,
                            googleMap.getModel().displayModel.getTileSize())));
        }

    }
  }


  
  private void updateUnderlay(MapView googleMap) {
    Log.d(TAG,"updating underlay");
    try {
      underlay.update(googleMap);
      } catch (IllegalStateException e) {
        Log.d(TAG,"Illegal state exception whilst updating underlay polyline");
      }
  }



  /**
   * Updates the start and end markers.
   * 
   * @param googleMap the google map
   */
  protected void updateStartAndEndMarkers(MapView googleMap) {
    // Add the end marker
    if (showEndMarker) {
      for (int i = locations.size() - 1; i >= 0; i--) {
        CachedLocation cachedLocation = locations.get(i);
        if (cachedLocation.valid) {
            //TODO: non-draggable ?
            Drawable drawable = context.getResources().getDrawable(R.drawable.red_dot);
            Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
            Marker marker = new Marker(cachedLocation.getLatLong(), bitmap, (int) MARKER_X_ANCHOR, (int) MARKER_Y_ANCHOR);
            this.endMarker = marker;
            googleMap.getLayerManager().getLayers().add(this.endMarker);
          break;
        }
      }
    }

    // Add the start marker
    for (int i = 0; i < locations.size(); i++) {
      CachedLocation cachedLocation = locations.get(i);
      if (cachedLocation.valid) {
          Drawable drawable = context.getResources().getDrawable(R.drawable.green_dot);
          Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
          Marker marker = new Marker(cachedLocation.getLatLong(), bitmap, (int) MARKER_X_ANCHOR, (int) MARKER_Y_ANCHOR);
          this.startMarker = marker;
          googleMap.getLayerManager().getLayers().add(this.startMarker);
        break;
      }
    }
  }

  /**
   * Updates the waypoints.
   * 
   * @param googleMap the google map.
   */
  protected void updateWaypoints(MapView googleMap) {
    synchronized (waypoints) {
      for (Waypoint waypoint : waypoints) {
        Location location = waypoint.getLocation();
        LatLong latLng = new LatLong(location.getLatitude(), location.getLongitude());
        int drawableId = waypoint.getType() == Waypoint.TYPE_STATISTICS ? R.drawable.yellow_pushpin
            : R.drawable.blue_pushpin;
          // TODO: add tile: String.valueOf(waypoint.getId())
          Drawable drawable = context.getResources().getDrawable(drawableId);
          Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
          Marker marker = new Marker(latLng, bitmap, (int) MARKER_X_ANCHOR, (int) MARKER_Y_ANCHOR);
          googleMap.getLayerManager().getLayers().add(marker);
      }
    }
  }

  public void addUnderlay(StaticOverlay underlay) {
    if (underlay !=null) {
      Log.d(TAG, "adding underlay");
    }
    this.underlay = underlay;
    
  }
}
