package org.cowboycoders.location;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocationUtilsTest {

    private static final double TINY = 1e-3;
    private static final double COARSE = 0.5;

    private static final LatLongAlt landsEnd = new LatLongAlt(50.0664, -5.7147, 0.0);
    private static final LatLongAlt johnoGroats = new LatLongAlt(58.6439, -3.07, 0.0);

    @Test
    public void testGradient() {
        // No height difference between points
        assertEquals(0.0, LocationUtils.getLocalisedGradient(landsEnd, johnoGroats), TINY);

        // Height between points
        LatLongAlt raisedPoint = new LatLongAlt(58.6439, -3.07, 1000.0);
        assertEquals(0.103, LocationUtils.getLocalisedGradient(landsEnd, raisedPoint), TINY);

        // Under the sea
        LatLongAlt sunkenPoint = new LatLongAlt(58.6439, -3.07, -1000.0);
        assertEquals(-0.103, LocationUtils.getLocalisedGradient(landsEnd, sunkenPoint), TINY);
    }

    @Test
    public void testDistance() {
        assertEquals(968853.0, LocationUtils.getDistance(landsEnd, johnoGroats), COARSE);
    }

}
