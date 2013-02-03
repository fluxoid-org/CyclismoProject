package org.cowboycoders.cyclisimo;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

import org.cowboycoders.cyclisimo.MapOverlay.CachedLocation;

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
