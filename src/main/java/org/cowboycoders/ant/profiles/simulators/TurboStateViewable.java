package org.cowboycoders.ant.profiles.simulators;

import org.cowboycoders.ant.profiles.fitnessequipment.pages.TrackResistance;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.WindResistance;

import java.math.BigDecimal;

/**
 * Read-only view of the state
 * Created by fluxoid on 16/02/17.
 */
public interface TurboStateViewable {
    int getPower();

    int getCadence();

    BigDecimal getSpeed();

    Athlete getAthlete();

    BigDecimal getBikeWeight();

    BigDecimal getGearRatio();

    FecTurboState.OperationState getState();

    TrackResistance getTrackResistance();

    WindResistance getWindResistance();

    Integer getHeartRate();

    int getDistance();
}
