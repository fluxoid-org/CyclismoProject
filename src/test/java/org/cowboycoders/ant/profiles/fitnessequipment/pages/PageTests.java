package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Config;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by fluxoid on 19/01/17.
 */
public class PageTests {

    // Payload gen just happens to align with the received response, but it is really for
    // generating requests

    @Test
    public void testBasicResistance() {
        byte[] data = PayloadGen.getSetBasicResistance(new BigDecimal(100));
        PercentageResistance resistance = new PercentageResistance(data);
        assertEquals( new BigDecimal(100).doubleValue(), resistance.getResistance().doubleValue(), 0.0001);
    }

    @Test
    public void requestCalibration() {
        byte data[] = PayloadGen.getRequestCalibration(true,true);
        CalibrationResponse calib = new CalibrationResponse(data);
        assertTrue(calib.isSpinDownSuccess());
        assertTrue(calib.isZeroOffsetSuccess());
    }

    @Test
    public void requestCalibration2() {
        byte data[] = PayloadGen.getRequestCalibration(false,false);
        CalibrationResponse calib = new CalibrationResponse(data);
        assertFalse(calib.isSpinDownSuccess());
        assertFalse(calib.isZeroOffsetSuccess());
    }

    @Test
    public void testTargetPower() {
        double expected = 123.25; //resolution 0.25
        byte [] data = PayloadGen.getSetTargetPower(new BigDecimal(expected));
        TargetPower page = new TargetPower(data);
        assertEquals(expected,page.getTargetPower().doubleValue(), 0.001);
    }

    @Test
    public void testTrackResistance() {
        double coeff = 0.0127;
        double grad = 21; //%
        byte [] data = PayloadGen.getSetTrackResistance(new BigDecimal(grad), new BigDecimal(coeff));
        TrackResistance page = new TrackResistance(data);
        assertEquals(coeff, page.getCoefficientRollingResistance().doubleValue(), 0.001);
        assertEquals(grad, page.getGradient().doubleValue(), 0.001);
    }

    @Test
    public void testUserConfig(){
        double uWeight = 80.5; //kg
        double bWeight = 7.0;
        double wheelDia = 0.72;
        double gearRatio = 1.0;
        byte data [] = PayloadGen.getSetUserConfiguration(
                new BigDecimal(uWeight),
                new BigDecimal(bWeight),
                new BigDecimal(wheelDia),
                new BigDecimal(gearRatio)
        );
        ConfigPage page = new ConfigPage(data);
        Config config = page.getConfig();
        assertEquals(uWeight, config.getUserWeight().doubleValue(), 0.001);
        assertEquals(bWeight, config.getBicycleWeight().doubleValue(), 0.001);
        assertEquals(wheelDia, config.getBicycleWheelDiameter().doubleValue(), 0.001);
        // the resolution on gear ratio aint so good as it goes through a 0.003 divide with 0dp truncation
        assertEquals(gearRatio, config.getGearRatio().doubleValue(), 0.1);
    }

    @Test
    public void testWind() {

        double coeff = 0.4;
        byte windspeed = 100;
        double draftingFactor = 0.5;
        byte [] data = PayloadGen.getSetWindResistance(new BigDecimal(coeff), windspeed, new BigDecimal(draftingFactor));
        WindResistance page = new WindResistance(data);
        assertEquals(coeff, page.getWindResitanceCoefficent().doubleValue(), 0.01);
        assertEquals(windspeed, page.getWindSpeed());
        assertEquals(draftingFactor, page.getDraftingFactor().doubleValue(), 0.001);
    }







}
