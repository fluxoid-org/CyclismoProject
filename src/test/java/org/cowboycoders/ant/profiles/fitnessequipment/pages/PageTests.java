package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Config;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.fitnessequipment.Capabilities;
import org.cowboycoders.ant.profiles.fitnessequipment.CapabilitiesBuilder;

import org.cowboycoders.ant.profiles.pages.CommonCommandPage;
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

    @Test
    public void testBikeData() {
        // encode-decode test
        final int cadence = 123;
        final int power = 755;
        byte [] data = new byte[8];
        new BikeData.BikeDataPayload()
                .setLapFlag(true)
                .setCadence(cadence)
                .setState(Defines.EquipmentState.READY)
                .setPower(power)
                .setType(Defines.EquipmentType.BIKE)
                .encode(data);
        BikeData page = new BikeData(data);
        assertEquals(cadence, page.getCadence());
        assertEquals(power, page.getPower());
        assertEquals(Defines.EquipmentState.READY, page.getState());
        assertTrue(page.isLapToggled());
        assertEquals(Defines.EquipmentType.BIKE, page.getEquipmentType());

    }

    @Test
    public void encodeDecodeCalibrationInProgress() {
        final BigDecimal temp =  new BigDecimal(75.5);
        final BigDecimal speed = new BigDecimal(25.5);
        final int spinDown = 2000; //ms
        byte [] data = new byte[8];
        new CalibrationProgress.CalibrationProgressPayload()
                .setOffsetPending(true)
                .setSpinDownPending(true)
                .setTargetSpeed(speed)
                .setTemp(temp)
                .setTargetSpinDownTime(spinDown)
                .setTempState(Defines.TemperatureCondition.CURRENT_TEMPERATURE_OK)
                .setSpeedState(Defines.SpeedCondition.CURRENT_SPEED_OK)
                .encode(data);
        CalibrationProgress page = new CalibrationProgress(data);
        assertEquals(true, page.isOffsetPending());
        assertEquals(true, page.isSpinDownPending());
        assertEquals(speed.doubleValue(), page.getTargetSpeed().doubleValue(), 0.1);
        assertEquals(temp.doubleValue(), page.getTemp().doubleValue(), 0.5);
        assertEquals(spinDown, (long) page.getTargetSpinDownTime());
        assertEquals(Defines.SpeedCondition.CURRENT_SPEED_OK, page.getSpeedState());
        assertEquals(Defines.TemperatureCondition.CURRENT_TEMPERATURE_OK, page.getTempState());
    }

    @Test
    public void encodeDecodeCalibrationResponse() {
        final int spinDownTime = 1234;
        final int zeroOffset = 5678;
        final double temp = 75.5;
        byte [] data = new byte[8];
        new CalibrationResponse.CalibrationResponsePayload()
                .setSpinDownSuccess(true)
                .setZeroOffsetSuccess(true)
                .setSpinDownTime(spinDownTime)
                .setZeroOffset(zeroOffset)
                .setTemp(new BigDecimal(temp))
                .encode(data);
        CalibrationResponse page = new CalibrationResponse(data);
        assertEquals(temp, page.getTemp().doubleValue(), 0.25);
        assertEquals(spinDownTime, (int) page.getSpinDownTime());
        assertEquals(zeroOffset, (int) page.getZeroOffset());
        assertTrue(page.isZeroOffsetSuccess());
        assertTrue(page.isSpinDownSuccess());
    }

    @Test
    public void encodeDecodeCapabilities() {
        final int maxResistance = 1234;
        Capabilities cap = new CapabilitiesBuilder()
                .setMaximumResistance(maxResistance)
                .setBasicResistanceModeSupport(true)
                .setSimulationModeSupport(true)
                .setTargetPowerModeSupport(true)
                .createCapabilities();
        byte [] packet = new byte[8];
         new CapabilitiesPage.CapabilitiesPayload()
                .setCapabilites(cap)
                .encode(packet);
         CapabilitiesPage page = new CapabilitiesPage(packet);
         assertEquals(maxResistance, (int) page.getCapabilites().getMaximumResistance());
         assertTrue(page.getCapabilites().isBasicResistanceModeSupported());
         assertTrue(page.getCapabilites().isSimulationModeSupported());
         assertTrue(page.getCapabilites().isTargetPowerModeSupported());

    }

    @Test
    public void encodeDecodeWindStatus() {
        Command.WindStatusBuilder builder = new Command.WindStatusBuilder();
        final double draftingFactor = 1.00;
        final double windResist = 0.51;
        final int windSpeed = -127;
        final int seqNum = 123;
        builder.setDraftingFactor(new BigDecimal(1.00));
        builder.setWindResistanceCoefficient(new BigDecimal(windResist));
        builder.setWindSpeed(windSpeed);
        CommonCommandPage.CommandStatus status = new CommonCommandPage.CommandStatusBuilder()
                .setStatus(Defines.Status.PASS)
                .setLastReceivedSequenceNumber(seqNum)
                .createCommandStatus();
        builder.setStatus(status);
        byte [] packet = new byte[8];
        builder.encode(packet);
        Command page = new Command(packet);
        Command.WindStatus recv = (Command.WindStatus) page.getFitnessStatus();
        assertEquals(draftingFactor, recv.getDraftingFactor().doubleValue(), 0.001);
        assertEquals(windResist, recv.getWindResistanceCoefficient().doubleValue(), 0.001);
        assertEquals(windSpeed, (int) recv.getWindSpeed());
        assertEquals(Defines.Status.PASS, recv.getStatus());
        assertEquals(seqNum, recv.getLastReceivedSequenceNumber());


    }

    @Test
    public void encodeDecodeTerrainStatus() {
        Command.TerrainStatusBuilder builder = new Command.TerrainStatusBuilder();
        final double grade = 23.5;
        final double coeff = 0.004;
        final int seqNum = 123;
        builder.setGrade(new BigDecimal(grade));
        builder.setRollingResistanceCoefficient(new BigDecimal(coeff));
        CommonCommandPage.CommandStatus status = new CommonCommandPage.CommandStatusBuilder()
                .setStatus(Defines.Status.PASS)
                .setLastReceivedSequenceNumber(seqNum)
                .createCommandStatus();
        builder.setStatus(status);
        byte [] packet = new byte[8];
        builder.encode(packet);
        Command page = new Command(packet);
        Command.TerrainStatus recv = (Command.TerrainStatus) page.getFitnessStatus();
        assertEquals(grade, recv.getGrade().doubleValue(), 0.001);
        assertEquals(coeff, recv.getRollingResistanceCoefficient().doubleValue(), 0.001);
        assertEquals(Defines.Status.PASS, recv.getStatus());
        assertEquals(seqNum, recv.getLastReceivedSequenceNumber());


    }

    @Test
    public void encodeDecodeTargetPower() {
        Command.TargetPowerStatusBuilder builder = new Command.TargetPowerStatusBuilder();
        final int targetPower = 1337;
        final int seqNum = 123;
        builder.setTargetPower(new BigDecimal(targetPower));
        CommonCommandPage.CommandStatus status = new CommonCommandPage.CommandStatusBuilder()
                .setStatus(Defines.Status.PASS)
                .setLastReceivedSequenceNumber(seqNum)
                .createCommandStatus();
        builder.setStatus(status);
        byte [] packet = new byte[8];
        builder.encode(packet);
        Command page = new Command(packet);
        Command.TargetPowerStatus recv = (Command.TargetPowerStatus) page.getFitnessStatus();
        assertEquals((double) targetPower, recv.getTargetPower().doubleValue(), 0.01);
        assertEquals(Defines.Status.PASS, recv.getStatus());
        assertEquals(seqNum, recv.getLastReceivedSequenceNumber());
    }

    @Test
    public void encodeDecodeResistance() {
        Command.ResistanceStatusBuilder builder = new Command.ResistanceStatusBuilder();
        final double resistance = 12.5;
        final int seqNum = 123;
        builder.setTotalResistance(new BigDecimal(resistance));
        CommonCommandPage.CommandStatus status = new CommonCommandPage.CommandStatusBuilder()
                .setStatus(Defines.Status.PASS)
                .setLastReceivedSequenceNumber(seqNum)
                .createCommandStatus();
        builder.setStatus(status);
        byte [] packet = new byte[8];
        builder.encode(packet);
        Command page = new Command(packet);
        Command.ResistanceStatus recv = (Command.ResistanceStatus) page.getFitnessStatus();
        assertEquals(resistance, recv.getTotalResistance().doubleValue(), 0.01);
        assertEquals(Defines.Status.PASS, recv.getStatus());
        assertEquals(seqNum, recv.getLastReceivedSequenceNumber());
    }







}
