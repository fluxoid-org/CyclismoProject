package org.cowboycoders.cyclismo.services;

import android.location.Criteria;

/**
 * Wraps a location provider to our interface @see LocationProvider
 *
 * Created by fluxoid on 22/03/15.
 */
public class LocationProviderWrapper implements LocationProvider {

    private android.location.LocationProvider wrapped;
    public LocationProviderWrapper(android.location.LocationProvider toWrap) {
        if (toWrap == null) throw new NullPointerException("location provider may not be null");
        wrapped = toWrap;
    }
    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public boolean meetsCriteria(Criteria criteria) {
        return wrapped.meetsCriteria(criteria);
    }

    @Override
    public boolean requiresNetwork() {
        return wrapped.requiresNetwork();
    }

    @Override
    public boolean requiresSatellite() {
        return wrapped.requiresSatellite();
    }

    @Override
    public boolean requiresCell() {
        return wrapped.requiresCell();
    }

    @Override
    public boolean hasMonetaryCost() {
        return wrapped.hasMonetaryCost();
    }

    @Override
    public boolean supportsAltitude() {
        return wrapped.supportsAltitude();
    }

    @Override
    public boolean supportsSpeed() {
        return wrapped.supportsSpeed();
    }

    @Override
    public boolean supportsBearing() {
        return wrapped.supportsBearing();
    }

    @Override
    public int getPowerRequirement() {
        return wrapped.getPowerRequirement();
    }

    @Override
    public int getAccuracy() {
        return wrapped.getAccuracy();
    }

    public android.location.LocationProvider unwrap() {
        return wrapped;
    }
}