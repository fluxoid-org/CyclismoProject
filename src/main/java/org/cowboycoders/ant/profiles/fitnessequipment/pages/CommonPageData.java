package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Defines;

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


    private static final int LAP_OFFSET = 8;
    private static final int LAP_MASK = 0x80;
    private static final int STATE_MASK = 0x70;
    private static final int STATE_SHIFT = 4;
    private static final int STATE_OFFSET = 8;
    private static final int TYPE_FLAG_OFFSET = 1;
    private static final int TYPE_FLAG = 16;
    private static final int TYPE_MASK = 0x1F;
    private static final int TYPE_OFFSET = 2;

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
