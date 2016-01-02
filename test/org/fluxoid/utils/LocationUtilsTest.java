package org.fluxoid.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocationUtilsTest {

    private static final double TINY = 1e-3;
    private static final double COARSE = 0.5;

    private static final LatLongAlt landsEnd = new LatLongAlt(50.0664, -5.7147, 0.0);
    private static final LatLongAlt johnoGroats = new LatLongAlt(58.6439, -3.07, 0.0);

    // Two points about 50m apart
    private static final LatLongAlt SRC = new LatLongAlt(51.38026, 12.35125, 123);
    private static final LatLongAlt DST = new LatLongAlt(51.37993, 12.35073, 122);


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
        double maxSpacing = 2.0;

        // Interpolate between the two points
        List<LatLongAlt> points = LocationUtils.interpolateBetweenPoints(SRC, DST, maxSpacing);

        // Check that the interpolated points do not exceed the maximum spacing
        LatLongAlt last = SRC;
        for (LatLongAlt point: points) {
            assertTrue(maxSpacing >= LocationUtils.getDistance(last, point));
            last = point;
        }
    }

  @Test
  public void testGetLocationBetweenPointsValid() {
    LatLongAlt nowhere = LocationUtils.getLocationBetweenPoints(SRC, DST, 50.0, 0.0);
    assertEquals(SRC.getLatitude(), nowhere.getLatitude(), TINY);
    assertEquals(SRC.getLongitude(), nowhere.getLongitude(), TINY);
    assertEquals(SRC.getAltitude(), nowhere.getAltitude(), TINY);

    LatLongAlt halfway = LocationUtils.getLocationBetweenPoints(SRC, DST, 25.0, 1000.0);
    assertEquals(51.38010, halfway.getLatitude(), TINY);
    assertEquals(12.35100, halfway.getLongitude(), TINY);
    assertEquals(122.5, halfway.getAltitude(), COARSE);

    LatLongAlt end = LocationUtils.getLocationBetweenPoints(SRC, DST, 50.0, 1000.0);
    assertEquals(DST.getLatitude(), end.getLatitude(), TINY);
    assertEquals(DST.getLongitude(), end.getLongitude(), TINY);
    assertEquals(DST.getAltitude(), end.getAltitude(), COARSE);
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGetLocationBetweenPointsInvalid() {
    exception.expect(RuntimeException.class);
    // ~50m past the destination
    LocationUtils.getLocationBetweenPoints(SRC, DST, 100.0, 1000.0);
  }
}
