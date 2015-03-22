package org.cowboycoders.cyclisimo.services;

/**
 * Replicates LocationProvider from android
 * Created by fluxoid on 22/03/15.
 */
public interface LocationProvider {
        public static final int OUT_OF_SERVICE = 0;
        public static final int TEMPORARILY_UNAVAILABLE = 1;
        public static final int AVAILABLE = 2;

        public java.lang.String getName();

        public boolean meetsCriteria(android.location.Criteria criteria);

        public boolean requiresNetwork();

        public boolean requiresSatellite();

        public boolean requiresCell();

        public boolean hasMonetaryCost();

        public boolean supportsAltitude();

        public boolean supportsSpeed();

        public boolean supportsBearing();

        public int getPowerRequirement();

        public int getAccuracy();

}
