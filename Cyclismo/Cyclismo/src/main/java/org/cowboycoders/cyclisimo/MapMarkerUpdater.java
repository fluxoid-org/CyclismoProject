package org.cowboycoders.cyclisimo;

import android.graphics.drawable.Drawable;
import android.location.Location;

import org.cowboycoders.cyclisimo.content.Waypoint;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

public class MapMarkerUpdater {
    private final MarkerSource markerSource;

    public MapMarkerUpdater(MarkerSource staticOverlay) {
        this.markerSource = staticOverlay;
    }

    /**
     * Updates the start and end markers.
     *
     * @param googleMap the google map
     */
    protected void updateStartAndEndMarkers(MapView googleMap) {
        // Add the end marker
        if (markerSource.isShowEndMarker()) {
            for (int i = markerSource.getLocations().size() - 1; i >= 0; i--) {
                MapOverlay.CachedLocation cachedLocation = markerSource.getLocations().get(i);
                if (cachedLocation.isValid()) {
                    Drawable drawable = markerSource.getStopMarker();
                    Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
                    Marker marker = new Marker(cachedLocation.getLatLong(),
                            bitmap, (int) markerSource.getMarkerXAnchor() * drawable.getIntrinsicWidth(),
                            (int) markerSource.getMarkerYAnchor() * drawable.getIntrinsicHeight());
                    googleMap.getLayerManager().getLayers().add(marker);
                    break;
                }
            }
        }

        // Add the start marker
        for (int i = 0; i < markerSource.getLocations().size(); i++) {
            MapOverlay.CachedLocation cachedLocation = markerSource.getLocations().get(i);
            if (cachedLocation.isValid()) {
                Drawable drawable = markerSource.getStartMarker();
                Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
                Marker marker = new Marker(cachedLocation.getLatLong(), bitmap,
                        (int) markerSource.getMarkerXAnchor() * drawable.getIntrinsicWidth(),
                        (int) markerSource.getMarkerYAnchor() * drawable.getIntrinsicHeight());
                googleMap.getLayerManager().getLayers().add(marker);
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
        synchronized (markerSource.getWaypoints()) {
            for (Waypoint waypoint : markerSource.getWaypoints()) {
                Location location = waypoint.getLocation();
                LatLong latLng = new LatLong(location.getLatitude(), location.getLongitude());
                int drawableId = waypoint.getType() == Waypoint.TYPE_STATISTICS ? R.drawable.yellow_pushpin
                        : R.drawable.blue_pushpin;
                // TODO: title
                Drawable drawable = markerSource.getWaypoint(waypoint);
                Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
                Marker marker = new Marker(latLng, bitmap,
                        (int) markerSource.getWaypointXAnchor() * drawable.getIntrinsicWidth(),
                        (int) markerSource.getWaypointYAnchor() * drawable.getIntrinsicHeight());
                googleMap.getLayerManager().getLayers().add(marker);
            }
        }
    }
}