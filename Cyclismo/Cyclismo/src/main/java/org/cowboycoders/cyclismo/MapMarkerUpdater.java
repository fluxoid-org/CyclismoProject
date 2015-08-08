package org.cowboycoders.cyclismo;

import android.graphics.drawable.Drawable;
import android.location.Location;

import org.cowboycoders.cyclismo.content.Waypoint;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Marker;

import java.util.HashMap;

public class MapMarkerUpdater {
    private final MarkerSource markerSource;
    private Marker endMarker;
    private Marker startMarker;
    private HashMap<Drawable, Bitmap> cache = new HashMap<Drawable, Bitmap>();

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
                    Bitmap bitmap = getBitMap(drawable);
                    Marker marker = new Marker(cachedLocation.getLatLong(),
                            bitmap,
                            (int) ((bitmap.getWidth() / 2) - markerSource.getMarkerXAnchor() * bitmap.getWidth()),
                            (int) ((bitmap.getHeight() / 2) - markerSource.getMarkerYAnchor() * bitmap.getHeight()));
                    googleMap.getLayerManager().getLayers().add(marker);
                    this.endMarker = marker;
                    break;
                }
            }
        }

        // Add the start marker
        for (int i = 0; i < markerSource.getLocations().size(); i++) {
            MapOverlay.CachedLocation cachedLocation = markerSource.getLocations().get(i);
            if (cachedLocation.isValid()) {
                Drawable drawable = markerSource.getStartMarker();
                Bitmap bitmap = getBitMap(drawable);
                Marker marker = new Marker(cachedLocation.getLatLong(), bitmap,
                        (int) ((bitmap.getWidth() / 2) - markerSource.getMarkerXAnchor() * bitmap.getWidth()),
                        (int) ((bitmap.getHeight() / 2) - markerSource.getMarkerYAnchor() * bitmap.getHeight()));
                googleMap.getLayerManager().getLayers().add(marker);
                this.startMarker = marker;
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
                // TODO: title
                Drawable drawable = markerSource.getWaypoint(waypoint);
                Bitmap bitmap = getBitMap(drawable);
                Marker marker = new Marker(latLng, bitmap,
                        (int) ((bitmap.getWidth() / 2) - markerSource.getWaypointXAnchor() * bitmap.getWidth()),
                        (int) ((bitmap.getHeight() / 2) - markerSource.getWaypointYAnchor() * bitmap.getHeight()));
                googleMap.getLayerManager().getLayers().add(marker);
            }
        }
    }

    public Layer getEndMarker() {
        return endMarker;
    }

    public Layer getStartMarker() {
        return startMarker;
    }

    public Bitmap getBitMap(Drawable drawable) {
        Bitmap result = cache.get(drawable);
        if (result != null) {
            return result;
        }
        result = AndroidGraphicFactory.convertToBitmap(drawable);
        result.incrementRefCount();
        cache.put(drawable, result);
        return result;
    }

    public void destroy() {
        // FIXME: incrementing ref counts of bitmaps seems to help with internal mapsforge null pointer
        // errors, but not eliminate the problem. We might be doing something unsafe somewhere!
        for (Bitmap bm : cache.values()) {
            bm.decrementRefCount();
        }
    }
}