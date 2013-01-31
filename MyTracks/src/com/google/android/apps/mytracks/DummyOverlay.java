package com.google.android.apps.mytracks;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Doesn't actually update the map
 * @author will
 *
 */
public class DummyOverlay extends MapOverlay {

  public DummyOverlay(Context context) {
    super(context);
  }
  
  
  /**
   * Ensure we don't accidently update map
   * Updates the track, start and end markers, and waypoints.
   * 
   * @param googleMap the google map
   * @param paths the paths
   * @param reload true to reload all points
   */
  public void update(GoogleMap googleMap, ArrayList<Polyline> paths, boolean reload) {
    List<CachedLocation> locations = getLocations();
    BlockingQueue<CachedLocation> pendingLocations = getPendingLocations();
    synchronized (locations) {
      pendingLocations.drainTo(locations);
      }
    }
  
  /**
   * Updates list of locations
   */
  public void update() {
    update(null,null,true);
  }
  
  

}
