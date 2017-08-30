package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 29/12/16.
 */
public class PayloadGen {

    public static final int NULL_BYTE = -1;
    public static final int CALIBRATION_CMD_ID = 1;
    public static final int ZERO_OFFSET_CALIBRATION_MASK = 0x40;
    public static final byte SPIN_DOWN_CALIBRATION_MASK = (byte) 128;
    public static final int NULL_GRADIENT = 65535;
    public static final int NULL_COEFF = 255;

    /**
     *
     * @param zeroOffsetCalibration offset from zero
     * @param spinDownCalibration request spindown time in ms
     * @return
     */
    public static byte[] getRequestCalibration(final boolean zeroOffsetCalibration, final boolean spinDownCalibration) {
        final byte[] array = {CALIBRATION_CMD_ID, 0, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE};
        if (zeroOffsetCalibration) {
            array[1] |= ZERO_OFFSET_CALIBRATION_MASK;
        }
        if (spinDownCalibration) {
            array[1] |= SPIN_DOWN_CALIBRATION_MASK;
        }
        return array;
    }

    /**
     *
     * @param totalResistance Percentage of maximum resistance to be applied.
     *                        Units: %. Valid range: 0% - 100%. Resolution: 0.5%
     */
    public static byte[] getSetBasicResistance(final BigDecimal totalResistance) {
        final byte[] array = { (byte) Defines.CommandId.BASIC_RESISTANCE.getIntValue(), NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE };
        array[7] = totalResistance.multiply(new BigDecimal("2")).setScale(0, RoundingMode.HALF_UP).byteValue();
        return array;
    }


    /**
     *
     * @param targetPower 0-1000w (0.25W resolution)
     */
    public static byte[] getSetTargetPower(final BigDecimal targetPower) {
        final byte[] array = { (byte)Defines.CommandId.TARGET_POWER.getIntValue(), NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, 0, 0 };
        BitManipulation.PutUnsignedNumIn2LeBytes(array, 6, targetPower.multiply(new BigDecimal("4")).setScale(0, RoundingMode.HALF_UP).intValue());
        return array;
    }

    /**
     *
     * @param gradient -200% to 200%, Resolution: 0.01
     * @param coeffRollingResistance Valid range: 0 - 0.0127. Resolution: 5x10^-5
     * @return
     */
    public static byte[] getSetTrackResistance(final BigDecimal gradient, final BigDecimal coeffRollingResistance) {
        final byte[] array = { (byte)Defines.CommandId.TRACK_RESISTANCE.getIntValue(), NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE };
        int gradientAsInt;
        if (gradient.intValue() != NULL_GRADIENT) {
            // add 200 to get gradient in positive range
            gradientAsInt = gradient.add(new BigDecimal(200)).multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();
        }
        else {
            gradientAsInt = NULL_GRADIENT;
        }
        byte byteValue;
        if (coeffRollingResistance.intValue() != NULL_COEFF) {
            byteValue = coeffRollingResistance.multiply(new BigDecimal("20000")).setScale(0, RoundingMode.HALF_UP).byteValue();
        }
        else {
            byteValue = (byte) NULL_COEFF;
        }
        BitManipulation.PutUnsignedNumIn2LeBytes(array, 5, gradientAsInt);
        array[7] = byteValue;
        return array;
    }

    /**
     *
     * @param userWeight 0 to 655.34 kg
     * @param bikeWeight 0 to 50kg
     * @param wheelDiameter 0 to 2.54m
     * @param gearRatio 0.03 to 7.65
     */
    public static byte[] getSetUserConfiguration(final BigDecimal userWeight, final BigDecimal bikeWeight, final BigDecimal wheelDiameter, final BigDecimal gearRatio) {
        final byte[] array = { 55, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, 0 };
        int intValue;
        if (userWeight.intValue() != 65535) {
            intValue = userWeight.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();
        }
        else {
            intValue = 65535;
        }
        int intValue2;
        if (bikeWeight.intValue() != 4095) {
            intValue2 = bikeWeight.multiply(new BigDecimal("20")).setScale(0, RoundingMode.HALF_UP).intValue();
        }
        else {
            intValue2 = 4095;
        }
        byte byteValue;
        if (wheelDiameter.intValue() != 255) {
            byteValue = wheelDiameter.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).byteValue();
        }
        else {
            byteValue = NULL_BYTE;
        }
        byte byteValue2;
        if (gearRatio.compareTo(new BigDecimal("0")) != 0) {
            byteValue2 = gearRatio.divide(new BigDecimal("0.03"), 0, RoundingMode.HALF_UP).byteValue();
        }
        else {
            byteValue2 = 0;
        }
        BitManipulation.PutUnsignedNumIn2LeBytes(array, 1, intValue);
        BitManipulation.PutUnsignedNumInUpper1And1HalfLeBytes(array, 4, intValue2);
        array[6] = byteValue;
        array[7] = byteValue2;
        return array;
    }

    /**
     *
     * @param coeff wind resistance coefficent, 0 kg/m to 1.86 kg/m. Calculated from:
     *              (frontal surface area) * (dragCoefficient) * (airDensity)
     *              typical example : 0.40 * 1.00 * 1.275
     * @param windSpeed -127 to 127 km/h
     * @param draftingFactor 0 to 1.00
     * @return
     */
    public static byte[] getSetWindResistance(final BigDecimal coeff, byte windSpeed, final BigDecimal draftingFactor) {
        final byte[] array = { (byte)Defines.CommandId.WIND_RESISTANCE.getIntValue(), NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE, NULL_BYTE };
        byte byteValue;
        if (coeff.intValue() != 255) {
            byteValue = coeff.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).byteValue();
        }
        else {
            byteValue = NULL_BYTE;
        }
        byte byteValue2;
        if (draftingFactor.intValue() != 255) {
            byteValue2 = draftingFactor.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).byteValue();
        }
        else {
            byteValue2 = NULL_BYTE;
        }
        if (windSpeed != NULL_BYTE) {
            windSpeed += 127;
        }
        array[5] = byteValue;
        array[6] = windSpeed;
        array[7] = byteValue2;
        return array;
    }
}
