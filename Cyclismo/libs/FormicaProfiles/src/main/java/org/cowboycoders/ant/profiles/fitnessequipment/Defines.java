package org.cowboycoders.ant.profiles.fitnessequipment;

import java.util.EnumSet;

/**
 * Created by fluxoid on 29/12/16.
 */
public class Defines {

    public enum CommandId
    {
        BASIC_RESISTANCE(48),
        NO_CONTROL_PAGE_RECEIVED(255),
        TARGET_POWER(49),
        TRACK_RESISTANCE(51),
        UNRECOGNIZED(-1),
        WIND_RESISTANCE(50);

        private int intValue;

        private CommandId(final int intValue) {
            this.intValue = intValue;
        }

        public static CommandId getValueFromInt(final int intValue) {
            for (final CommandId commandId : values()) {
                if (commandId.getIntValue() == intValue) {
                    return commandId;
                }
            }
            final CommandId unrecognized = CommandId.UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }

    public enum EquipmentState
    {
        ASLEEP_OFF(1),
        FINISHED_PAUSED(4),
        IN_USE(3),
        READY(2),
        UNRECOGNIZED(-1);

        private int intValue;

        private EquipmentState(final int intValue) {
            this.intValue = intValue;
        }

        public static EquipmentState getValueFromInt(final int intValue) {
            for (final EquipmentState equipmentState : values()) {
                if (equipmentState.getIntValue() == intValue) {
                    return equipmentState;
                }
            }
            final EquipmentState unrecognized = EquipmentState.UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }

    public enum EquipmentType
    {
        BIKE(21),
        CLIMBER(23),
        ELLIPTICAL(20),
        GENERAL(16),
        NORDICSKIER(24),
        ROWER(22),
        TRAINER(25),
        TREADMILL(19),
        UNKNOWN(-1),
        UNRECOGNIZED(-2);

        private int intValue;

        private EquipmentType(final int intValue) {
            this.intValue = intValue;
        }

        public static EquipmentType getValueFromInt(final int intValue) {
            for (final EquipmentType equipmentType : values()) {
                if (equipmentType.getIntValue() == intValue) {
                    return equipmentType;
                }
            }
            final EquipmentType unrecognized = EquipmentType.UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }

    public enum TemperatureCondition
    {
        CURRENT_TEMPERATURE_OK(2),
        CURRENT_TEMPERATURE_TOO_HIGH(3),
        CURRENT_TEMPERATURE_TOO_LOW(1),
        NOT_APPLICABLE(0),
        UNRECOGNIZED(-1);

        private int intValue;

        private TemperatureCondition(final int intValue) {
            this.intValue = intValue;
        }

        public static TemperatureCondition getValueFromInt(final int intValue) {
            for (final TemperatureCondition temperatureCondition : values()) {
                if (temperatureCondition.getIntValue() == intValue) {
                    return temperatureCondition;
                }
            }
            final TemperatureCondition unrecognized = TemperatureCondition.UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }

    public enum SpeedCondition
    {
        CURRENT_SPEED_OK(2),
        CURRENT_SPEED_TOO_LOW(1),
        NOT_APPLICABLE(0),
        UNRECOGNIZED(-1);

        private int intValue;

        private SpeedCondition(final int intValue) {
            this.intValue = intValue;
        }

        public static SpeedCondition getValueFromInt(final int intValue) {
            for (final SpeedCondition speedCondition : values()) {
                if (speedCondition.getIntValue() == intValue) {
                    return speedCondition;
                }
            }
            final SpeedCondition unrecognized = SpeedCondition.UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }

    public enum HeartRateDataSource
    {
        ANTPLUS_HRM(1),
        EM_5KHz(2),
        HAND_CONTACT_SENSOR(3),
        UNKNOWN(0),
        UNRECOGNIZED(-1);

        private int intValue;

        private HeartRateDataSource(final int intValue) {
            this.intValue = intValue;
        }

        public static HeartRateDataSource getValueFromInt(final int intValue) {
            for (final HeartRateDataSource heartRateDataSource : values()) {
                if (heartRateDataSource.getIntValue() == intValue) {
                    return heartRateDataSource;
                }
            }
            final HeartRateDataSource unrecognized = HeartRateDataSource.UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }

    public enum TrainerStatusFlag
    {
        BICYCLE_POWER_CALIBRATION_REQUIRED(Byte.BYTE0, 0x10),
        RESISTANCE_CALIBRATION_REQUIRED(Byte.BYTE0, 0x20),
        USER_CONFIGURATION_REQUIRED(Byte.BYTE0, 0x40),
        MAXIMUM_POWER_LIMIT_REACHED(Byte.BYTE1, 0x1),
        MINIMUM_POWER_LIMIT_REACHED(Byte.BYTE1, 0x2);

        public static final int STATUS_OFFSET = 6;

        private enum Byte {
            BYTE0,
            BYTE1
        }

        private final int mask;
        private final Byte byt;

        private TrainerStatusFlag(final Byte byt, final int mask) {
            this.mask = mask;
            this.byt = byt;
        }

        public static EnumSet<TrainerStatusFlag> getEnumSet(byte[] packet) {
            byte b0 = packet[STATUS_OFFSET];
            byte b1 = packet[STATUS_OFFSET + 1];
            final EnumSet<TrainerStatusFlag> none = EnumSet.noneOf(TrainerStatusFlag.class);
            for (final TrainerStatusFlag trainerStatusFlag : values()) {
                final int mask = trainerStatusFlag.mask;
                if (trainerStatusFlag.byt == Byte.BYTE0) {
                    if ((mask & b0) == mask) {
                        none.add(trainerStatusFlag);
                    }
                } else {
                    if ((mask & b1) == mask) {
                        none.add(trainerStatusFlag);
                    }
                }

            }
            return none;
        }

        public static void encode(final byte[] packet, EnumSet<TrainerStatusFlag> vals) {
            byte b0 = packet[STATUS_OFFSET];
            byte b1 = packet[STATUS_OFFSET + 1];
            for (TrainerStatusFlag flag : vals) {
                if (flag.byt == Byte.BYTE0) {
                    b0 |= flag.mask;
                } else {
                    b1 |= flag.mask;
                }
            }
            packet[STATUS_OFFSET] = b0;
            packet[STATUS_OFFSET + 1] = b1;
        }


    }

    /**
     * These correspond to page numbers
     */
    public enum TorqueSource
    {

        UNRECOGNIZED(-3),
        TRAINER_TORQUE_DATA(26),

        // seperate power monitors (normally separated in ant+ code)
        CRANK_TORQUE_DATA(18),
        WHEEL_TORQUE_DATA(17),
        CTF_DATA(32); // Crank Torque Frequnency - eg. SRM

        private int intValue;

        private TorqueSource(final int intValue) {
            this.intValue = intValue;
        }

        public static TorqueSource getValueFromInt(final int intValue) {
            for (final TorqueSource torqueSource : values()) {
                if (torqueSource.getIntValue() == intValue) {
                    return torqueSource;
                }
            }
            final TorqueSource unrecognized = TorqueSource.UNRECOGNIZED;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }

    public enum Status
    {
        FAIL(1),
        NOT_SUPPORTED(2),
        PASS(0),
        PENDING(4),
        REJECTED(3),
        UNINITIALIZED(255),
        UNRECOGNIZED(-1);

        private int intValue;

        private Status(final int intValue) {
            this.intValue = intValue;
        }

        public static Status getValueFromInt(final int intValue) {
            for (final Status status : values()) {
                if (status.getIntValue() == intValue) {
                    return status;
                }
            }
            final Status unrecognized = Status.UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }




}
