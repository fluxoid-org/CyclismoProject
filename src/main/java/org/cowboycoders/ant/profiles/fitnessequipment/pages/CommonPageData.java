package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Defines;

import static org.cowboycoders.ant.profiles.BitManipulation.clearMaskedBits;

/**
 * Created by fluxoid on 29/12/16.
 */
public abstract class CommonPageData {


    public static abstract class Listener {
        /**
         * @param state cyles between true and false. A negation indicates a lap.
         */
        public void onLapFlagDecode(final boolean state) {
        }

        /**
         * @param state unknown state code
         */
        public void onStateDecode(final int state) {
        }

        /**
         * @param type
         */
        public void onFitnessTypeDecode(final int type) {
        }

    }

    /**
     * Each lap toggles the flag
     */
    public boolean isLapToggled() {
        return lapFlag;
    }


    public Defines.EquipmentState getState() {
        return state;
    }

    /**
     *
     * @return null, if not contained in packet.
     */
    public Defines.EquipmentType getEquipmentType() {
        return equipmentType;
    }


    private final boolean lapFlag;
    private final Defines.EquipmentState state;
    private final Defines.EquipmentType equipmentType;


    private static final int LAP_OFFSET = 7;
    private static final int LAP_MASK = 0x80;
    private static final int STATE_MASK = 0x70;
    private static final int STATE_SHIFT = 4;
    private static final int STATE_OFFSET = 7;
    private static final int TYPE_FLAG_OFFSET = 0;
    private static final int TYPE_FLAG = 16;
    private static final int TYPE_MASK = 0x1F;
    private static final int TYPE_OFFSET = 1;

    public static class CommonPagePayload {
        private boolean lapFlag = false;
        private Defines.EquipmentState state = Defines.EquipmentState.UNRECOGNIZED;
        private Defines.EquipmentType type = Defines.EquipmentType.UNKNOWN;

        public boolean isLapFlagSet() {
            return lapFlag;
        }

        public Defines.EquipmentState getState() {
            return state;
        }

        public Defines.EquipmentType getType() {
            return type;
        }

        public CommonPagePayload setLapFlag(boolean lapflag) {
            this.lapFlag = lapflag;
            return this;
        }

        public CommonPagePayload setState(Defines.EquipmentState state) {
            this.state = state;
            return this;
        }

        public CommonPagePayload setType(Defines.EquipmentType type) {
            this.type = type;
            return this;
        }

        public void encode(final byte[] packet) {
            if (lapFlag) {
                packet[LAP_OFFSET] |= LAP_MASK;
            } else {
                packet[LAP_OFFSET] = clearMaskedBits(packet[LAP_OFFSET], LAP_MASK);
            }
            int stateRaw = (state.getIntValue() << STATE_SHIFT) & STATE_MASK;
            packet[STATE_OFFSET] |= (byte) (0xff & stateRaw);
            // we may need to make this configurable
            packet[TYPE_FLAG_OFFSET] = TYPE_FLAG;
            packet[TYPE_OFFSET] = (byte) (0xff & (type.getIntValue() & TYPE_MASK));
        }
    }

    private static boolean intToBoolean(final int n) {
        return n != 0;
    }

    public CommonPageData(byte[] data) {
        this.lapFlag = (intToBoolean(LAP_MASK & data[LAP_OFFSET]));

        this.state = Defines.EquipmentState.getValueFromInt(((data[STATE_OFFSET] & STATE_MASK) >>> STATE_SHIFT));
        if (data[TYPE_FLAG_OFFSET] == TYPE_FLAG) {
            this.equipmentType = Defines.EquipmentType.getValueFromInt(data[TYPE_OFFSET] & TYPE_MASK);
        } else {
            this.equipmentType = null;
        }

    }

}
