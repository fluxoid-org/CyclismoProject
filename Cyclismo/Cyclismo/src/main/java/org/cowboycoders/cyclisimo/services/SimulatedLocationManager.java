package org.cowboycoders.cyclisimo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;

import org.cowboycoders.cyclisimo.R;

import java.util.HashSet;

/**
 * Manages simulated locations e.g from turbo trainer
 * Created by fluxoid on 22/03/15.
 */
public class SimulatedLocationManager {

    private final SimulatedLocationProvider provider;

    private final String TURBO_SERVICE_LOCATION_UPDATE;
    private final String TURBO_SERVICE_LOCATION_UPDATE_DATA;
    private HashSet<LocationListener> listeners = new HashSet<LocationListener>();
    private Location lastLocation = null;

    private final BroadcastReceiver turboServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TURBO_SERVICE_LOCATION_UPDATE)) {
                Location loc = intent.getParcelableExtra(TURBO_SERVICE_LOCATION_UPDATE_DATA);
                lastLocation = loc;
                for (LocationListener listener: listeners) {
                    listener.onLocationChanged(loc);
                }
            }
        }
    };

    public SimulatedLocationManager(Context context) {
        this.provider = new SimulatedLocationProvider(context);
        TURBO_SERVICE_LOCATION_UPDATE =
                context.getString(R.string.turbo_service_action_location_update);
        TURBO_SERVICE_LOCATION_UPDATE_DATA =
                context.getString(R.string.turbo_service_data_location_update);
        IntentFilter filter = new IntentFilter();
        filter.addAction(TURBO_SERVICE_LOCATION_UPDATE);
        context.registerReceiver(turboServiceReceiver, filter);
    }


    /**
     * @see android.location.LocationManager#isProviderEnabled(java.lang.String)
     */
    public boolean isProviderEnabled(String provider) {
        return true;
    }

    /**
     * @see android.location.LocationManager#getProvider(java.lang.String)
     */
    public LocationProvider getProvider(String name) {
        return provider;
    }

    /**
     * @see android.location.LocationManager#getLastKnownLocation(java.lang.String)
     */
    public Location getLastKnownLocation(String provider) {
        return lastLocation;
    }

    /**
     * @see android.location.LocationManager#requestLocationUpdates(java.lang.String,
     *      long, float, android.location.LocationListener)
     */
    public void requestLocationUpdates(
            String provider, long minTime, float minDistance, LocationListener listener) {
        // we ignore the other parameters for simplicity
        listeners.add(listener);
    }

    /**
     * @param listener
     * @see android.location.LocationManager#removeUpdates(android.location.LocationListener)
     */
    public void removeUpdates(LocationListener listener) {
        listeners.remove(listener);
    }
}
