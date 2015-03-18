package org.cowboycoders.location;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testInterpolateBetweenPoints() {
        // Two points about 50m apart, going down a slight hill
        LatLongAlt src = new LatLongAlt(51.38026, 12.35125, 123);
        LatLongAlt dst = new LatLongAlt(51.37993, 12.35073, 122);
        double maxSpacing = 2.0;

        // Interpolate between the two points
        List<LatLongAlt> points = LocationUtils.interpolateBetweenPoints(src, dst, maxSpacing);

        // Check that the interpolated points do not exceed the maximum spacing
        LatLongAlt last = src;
        for (LatLongAlt point: points) {
            assertTrue(maxSpacing >= LocationUtils.getDistance(last, point));
            last = point;
        }

    }

}
