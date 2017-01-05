package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.common.CounterUtils;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.cowboycoders.ant.profiles.pages.VirtualPageGenerator;

import java.util.EnumSet;
import java.util.List;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Created by fluxoid on 02/01/17.
 */
public class TrainerData extends CommonPageData {

    public static final int POWER_OFFSET = 4;
    public static final int EVENT_OFFSET = 2;
    public static final int INSTANT_POWER_OFFSET = 6;
    public static final int CADENCE_OFFSET = 3;

    private final int power;
    private final int instantPower;
    private final boolean powerAvailable;
    private final int events;

    /**
     * Accumulated power : running sum of instanteous power updated incremented on each update of event count
     * @return
     */
    public int getSumPower() {
        return power;
    }

    public int getInstantPower() {
        return instantPower;
    }

    public boolean isPowerAvailable() {
        return powerAvailable;
    }

    /**
     *
     * @return
     */
    public int getEventCount() {
        return events;
    }

    /**
     * @return in rpm
     */
    public Integer getCadence() {
        return cadence;
    }

    public EnumSet<Defines.TrainerStatusFlag> getTrainerStatus() {
        return trainerStatus;
    }

    private final Integer cadence;
    private final EnumSet<Defines.TrainerStatusFlag> trainerStatus;


    public TrainerData(byte [] packet) {
        super(packet);
        power = UnsignedNumFrom2LeBytes(packet, POWER_OFFSET);
        events = UnsignedNumFrom1LeByte(packet[EVENT_OFFSET]);
        instantPower = 0xfff & UnsignedNumFrom2LeBytes(packet, INSTANT_POWER_OFFSET);
        if (instantPower != UNSIGNED_INT12_MAX) {
            powerAvailable = true;
        } else {
            powerAvailable = false;
        }
        final int cadenceRaw = UnsignedNumFrom1LeByte(packet[CADENCE_OFFSET]);
        if (cadenceRaw != UNSIGNED_INT8_MAX) {
            cadence = cadenceRaw;
        } else {
            cadence = null;
        }
        trainerStatus = Defines.TrainerStatusFlag.getEnumSet(packet);

    }

    public long getSumPowerDelta(TrainerData old) {
        return CounterUtils.calcDelta(old.getSumPower(), getSumPower(), UNSIGNED_INT16_MAX);
    }

    public long getEventCountDelta(TrainerData old) {
        return CounterUtils.calcDelta(old.getEventCount(), getEventCount(), UNSIGNED_INT16_MAX);
    }



}
