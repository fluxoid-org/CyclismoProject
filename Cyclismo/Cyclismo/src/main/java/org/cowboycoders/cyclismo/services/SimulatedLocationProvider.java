package org.cowboycoders.cyclismo.services;


import android.content.Context;
import android.location.Criteria;

import org.cowboycoders.cyclismo.R;

/**
 * Created by fluxoid on 22/03/15.
 */
public class SimulatedLocationProvider implements LocationProvider {

    public static final String NAME = "Simulated Location";
    private final int accuracy; // in m

    public SimulatedLocationProvider(Context context) {
        this.accuracy = context.getResources().getInteger(R.integer.SIMULATED_LOCATION_ACCURACY);
    }
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean meetsCriteria(Criteria criteria) {
        return true;
    }

    @Override
    public boolean requiresNetwork() {
        return false;
    }

    @Override
    public boolean requiresSatellite() {
        return false;
    }

    @Override
    public boolean requiresCell() {
        return false;
    }

    @Override
    public boolean hasMonetaryCost() {
        return false;
    }

    @Override
    public boolean supportsAltitude() {
        return true;
    }

    @Override
    public boolean supportsSpeed() {
        return true;
    }

    @Override
    public boolean supportsBearing() {
        return false;
    }

    @Override
    public int getPowerRequirement() {
        return 0;
    }

    @Override
    public int getAccuracy() {
        return accuracy;
    }
}
