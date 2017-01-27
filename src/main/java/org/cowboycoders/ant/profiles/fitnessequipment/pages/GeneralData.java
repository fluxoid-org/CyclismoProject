package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * p16
 * Created by fluxoid on 02/01/17.
 */
public class GeneralData extends CommonPageData implements AntPage {

    // these are counters that overflow
    private static final int TIME_OFFSET = 2;
    private static final int DISTANCE_OFFSET = 3;

    private static final int META_OFFSET = 7;
    private static final int SPEED_OFFSET = 4;
    private static final int HR_OFFSET = 6; // heart rate
    private static final int HR_SOURCE_MASK = 0x3;
    private static final int DISTANCE_MASK = 0x4;
    private static final int VIRTUAL_SPEED_MASK = 0x8;

    public boolean isDistanceAvailable() {
        return distanceAvailable;
    }

    public int getTimeElapsed() {
        return timeElapsed;
    }

    public Integer getDistanceCovered() {
        return distanceCovered;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public Defines.HeartRateDataSource getHeartRateSource() {
        return heartRateSource;
    }

    public boolean isUsingVirtualSpeed() {
        return usingVirtualSpeed;
    }


    private final boolean distanceAvailable;
    private final int timeElapsed;
    private final Integer distanceCovered;
    private final BigDecimal speed;
    private final Integer heartRate;
    private final Defines.HeartRateDataSource heartRateSource;
    private final boolean usingVirtualSpeed;


    public GeneralData(byte [] packet) {
        super(packet);
        distanceAvailable = booleanFromU8(packet[META_OFFSET], DISTANCE_MASK);

        this.timeElapsed = UnsignedNumFrom1LeByte(packet[TIME_OFFSET]);

        if (distanceAvailable) {
            distanceCovered = UnsignedNumFrom1LeByte(packet[DISTANCE_OFFSET]);
        } else {
            // IMO null will lead to fewer hard to detect bugs
            distanceCovered = null;
        }

        final int speedRaw = UnsignedNumFrom2LeBytes(packet, SPEED_OFFSET);
        if (speedRaw != UNSIGNED_INT16_MAX) {
            speed = new BigDecimal(speedRaw).divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP);
        } else {
            speed = new BigDecimal(0);
        }
        
        final int heartRateRaw = UnsignedNumFrom1LeByte(packet[HR_OFFSET]);
        if (heartRateRaw == UNSIGNED_INT8_MAX) {
            heartRate = heartRateRaw;
        } else {
            heartRate = 0;
        }

        heartRateSource = Defines.HeartRateDataSource.getValueFromInt(HR_SOURCE_MASK & packet[META_OFFSET]);
        usingVirtualSpeed = booleanFromU8(packet[META_OFFSET], VIRTUAL_SPEED_MASK);
        
    }

    private int getDeltaUnscaled(GeneralData old) {
        if (old.timeElapsed > timeElapsed) {
            return (timeElapsed - old.timeElapsed) + UNSIGNED_INT8_MAX + 1;
        }
        return timeElapsed - old.timeElapsed;
    }

    /**
     * Seconds elapsed since last update
     * @param old page containing previous update
     * @return time in seconds since last update
     */
    public BigDecimal getTimeDelta(GeneralData old) {
        if (old == null) {
            return scaleTime(timeElapsed);
        }
        return scaleTime(getDeltaUnscaled(old));
    }

    private BigDecimal scaleTime(int old) {
        return new BigDecimal(old).divide(new BigDecimal(4), 2, RoundingMode.HALF_UP);
    }

    /**
     * You should only store a previous update with isDistanceAvailable equal to true
     * @param old page containing previous update
     * @return distance in m
     */
    public BigDecimal getDistanceDelta(GeneralData old) {
        if (old == null) {
            return scaleDistance(distanceCovered);
        }
        return scaleDistance(getDeltaUnscaled(old));
    }

    private BigDecimal scaleDistance(int distance) {
        return new BigDecimal(distance).divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP);
    }

}
