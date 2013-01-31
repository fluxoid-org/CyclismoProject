package com.google.android.apps.mytracks;

import com.google.android.apps.mytracks.MapOverlay.CachedLocation;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Up for deletion
 * @author will
 *
 */
public interface BaseOverlay {
  
  public void setBaseOverlayPaths(ArrayList<Polyline> paths);
  
  public ArrayList<Polyline> getBaseOverlayPaths();
  
  public List<CachedLocation> getLocations();

}
