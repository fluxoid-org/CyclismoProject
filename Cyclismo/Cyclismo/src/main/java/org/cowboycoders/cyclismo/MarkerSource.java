package org.cowboycoders.cyclismo;

import android.graphics.drawable.Drawable;

import org.cowboycoders.cyclismo.content.Waypoint;

import java.util.List;

/**
 * Created by fluxoid on 20/03/15.
 */
public interface MarkerSource {
    float getWaypointXAnchor();

    float getWaypointYAnchor();

    float getMarkerXAnchor();

    float getMarkerYAnchor();

    Drawable getWaypoint(Waypoint waypoint);

    Drawable getStopMarker();

    Drawable getStartMarker();

    List<Waypoint> getWaypoints();

    List<MapOverlay.CachedLocation> getLocations();

    boolean isShowEndMarker();
}
