package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.TimeOutDeltaValidator;
import org.cowboycoders.ant.profiles.common.utils.CounterUtils;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CounterBasedDecodable;
import org.cowboycoders.ant.profiles.common.decode.interfaces.PowerOnlyDecodable;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.RollOverVal;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 *
 * Seems to be used for EquipmentType.Trainer
 *
 * Isn't used for: EquipmentType.Bike
 *
 * Created by fluxoid on 02/01/17.
 */
public class TrainerData extends CommonPageData implements PowerOnlyDecodable, AntPage {

    public static final int PAGE_NUMBER = 25;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    private static final long TIMEOUT_DELTA = TimeUnit.SECONDS.toNanos(12);
    public static final int POWER_OFFSET = 3;
    public static final int EVENT_OFFSET = 1;
    public static final int INSTANT_POWER_OFFSET = 5;
    public static final int CADENCE_OFFSET = 2;

    private final int power;
    private final int instantPower;
    private final boolean powerAvailable;
    private final int events;
    private final long timestamp;

    public static class TrainerDataPayload extends CommonPagePayload implements AntPacketEncodable {
        private RollOverVal powerSum = new RollOverVal(UNSIGNED_INT16_MAX);
        private int instantPower = -1;
        private RollOverVal events = new RollOverVal(UNSIGNED_INT8_MAX);
        private Integer cadence;
        private EnumSet<Defines.TrainerStatusFlag> trainerStatusFlags = EnumSet.noneOf(Defines.TrainerStatusFlag.class);

        public Integer getCadence() {
            return cadence;
        }

        public TrainerDataPayload setCadence(Integer cadence) {
            this.cadence = cadence;
            return this;
        }

        public EnumSet<Defines.TrainerStatusFlag> getTrainerStatusFlags() {
            return trainerStatusFlags;
        }

        public TrainerDataPayload setTrainerStatusFlags(EnumSet<Defines.TrainerStatusFlag> trainerStatusFlags) {
            if (trainerStatusFlags == null) {
                throw new IllegalArgumentException("trainerStatusFlags cannot be null");
            }
            this.trainerStatusFlags = trainerStatusFlags;
            return this;
        }

        public long getPowerSum() {
            return powerSum.getValue();
        }

        public TrainerDataPayload setPowerSum(long powerSum) {
            this.powerSum.setValue(powerSum);
            return this;
        }

        public int getInstantPower() {
            return instantPower;
        }

        public TrainerDataPayload setInstantPower(int instantPower) {
            if (instantPower < 0 || instantPower > UNSIGNED_INT12_MAX) {
                throw new IllegalArgumentException("instant powerSum out of range");
            }
            this.instantPower = instantPower;
            return this;
        }

        public boolean isPowerAvailable() {
            return instantPower != -1;
        }


        public int getEvents() {
            return Math.toIntExact(events.getValue());
        }

        public TrainerDataPayload setEvents(int events) {
            this.events.setValue(events);
            return this;
        }

        @Override
        public TrainerDataPayload setLapFlag(boolean lapflag) {
            return (TrainerDataPayload) super.setLapFlag(lapflag);
        }

        @Override
        public TrainerDataPayload setState(Defines.EquipmentState state) {
            return (TrainerDataPayload) super.setState(state);
        }

        public void encode(final byte[] packet) {
            super.encode(packet);
            PutUnsignedNumIn1LeBytes(packet, PAGE_OFFSET, PAGE_NUMBER);
            PutUnsignedNumIn2LeBytes(packet, POWER_OFFSET, Math.toIntExact(powerSum.get()));
            PutUnsignedNumIn1LeBytes(packet, EVENT_OFFSET, Math.toIntExact(events.get()));
            int old = UnsignedNumFrom2LeBytes(packet, INSTANT_POWER_OFFSET);
            if (isPowerAvailable()) {
                PutUnsignedNumIn2LeBytes(packet, INSTANT_POWER_OFFSET, old | instantPower );
            } else {
                PutUnsignedNumIn2LeBytes(packet, INSTANT_POWER_OFFSET, old | UNSIGNED_INT12_MAX );
            }
            if (cadence == null) {
                PutUnsignedNumIn1LeBytes(packet, CADENCE_OFFSET, UNSIGNED_INT8_MAX);
            } else {
                PutUnsignedNumIn1LeBytes(packet, CADENCE_OFFSET, cadence);
            }
            Defines.TrainerStatusFlag.encode(packet, trainerStatusFlags);
        }
    }


    /**
     * Accumulated powerSum : running sum of instanteous powerSum updated on each increment of event count
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

    @Override
    public boolean isValidDelta(CounterBasedDecodable old) {
        return timeOutDeltaValidator.isValidDelta(old, this);
    }

    /**
     * @return in rpm
     */
    public Integer getCadence() {
        return cadence;
    }

    public EnumSet<Defines.TrainerStatusFlag> getStatusFlags() {
        return statusFlags;
    }

    private final Integer cadence;
    private final EnumSet<Defines.TrainerStatusFlag> statusFlags;
    private final TimeOutDeltaValidator timeOutDeltaValidator = new TimeOutDeltaValidator(TIMEOUT_DELTA);


    public TrainerData(byte [] packet) {
        super(packet);
        this.timestamp = System.nanoTime();
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
        statusFlags = Defines.TrainerStatusFlag.getEnumSet(packet);

    }


    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getSumPowerDelta(PowerOnlyDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT16_MAX, old.getSumPower(), getSumPower());
    }

    @Override
    public long getEventCountDelta(CounterBasedDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT8_MAX, old.getEventCount(), getEventCount());
    }


}
