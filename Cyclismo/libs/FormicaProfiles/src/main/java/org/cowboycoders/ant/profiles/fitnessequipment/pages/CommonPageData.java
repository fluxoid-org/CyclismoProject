package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.common.decode.interfaces.LapFlagDecodable;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;

import static org.cowboycoders.ant.profiles.BitManipulation.clearMaskedBits;

/**
 * Created by fluxoid on 29/12/16.
 */
public abstract class CommonPageData implements LapFlagDecodable {


    /**
     * Each lap toggles the flag
     */
    public boolean isLapToggled() {
        return lapFlag;
    }


    public Defines.EquipmentState getState() {
        return state;
    }


    private final boolean lapFlag;
    private final Defines.EquipmentState state;



    private static final int LAP_OFFSET = 7;
    private static final int LAP_MASK = 0x80;
    private static final int STATE_MASK = 0x70;
    private static final int STATE_SHIFT = 4;
    private static final int STATE_OFFSET = 7;


    public static class CommonPagePayload {
        private boolean lapFlag = false;
        private Defines.EquipmentState state = Defines.EquipmentState.UNRECOGNIZED;


        public boolean isLapFlagSet() {
            return lapFlag;
        }

        public Defines.EquipmentState getState() {
            return state;
        }


        public CommonPagePayload setLapFlag(boolean lapflag) {
            this.lapFlag = lapflag;
            return this;
        }

        public CommonPagePayload setState(Defines.EquipmentState state) {
            this.state = state;
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
        }
    }

    private static boolean intToBoolean(final int n) {
        return n != 0;
    }

    public CommonPageData(byte[] data) {
        this.lapFlag = (intToBoolean(LAP_MASK & data[LAP_OFFSET]));

        this.state = Defines.EquipmentState.getValueFromInt(((data[STATE_OFFSET] & STATE_MASK) >>> STATE_SHIFT));


    }

}
