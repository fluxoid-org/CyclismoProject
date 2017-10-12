package org.cowboycoders.ant.profiles.fs.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.fluxoid.utils.bytes.LittleEndianArray;

public class BeaconAuth extends CommonBeacon {


    private final int serialNumber;
    private final AuthMode authMode;
    private final boolean pairingEnabled;

    public BeaconAuth(byte[] data) {
        super(data);
        LittleEndianArray view = new LittleEndianArray(data);
        serialNumber = view.unsignedToInt(4, 4);
        authMode = AuthMode.decode(data[3]);
        pairingEnabled = BitManipulation.booleanFromU8(data[1], 0x8);
    }

    public AuthMode getAuthMode() {
        return authMode;
    }

    public boolean isPairingEnabled() {
        return pairingEnabled;
    }

    public int getSerialNumber() {
        return serialNumber;
    }


    public static class BeaconAuthPayload extends CommonBeaconPayload  {

        private int serialNumber;
        private AuthMode authMode;
        private boolean pairingEnabled = false;


        public BeaconAuthPayload() {
            setState(State.AUTH);
        }

        @Override
        public void encode(byte[] packet) {
            super.encode(packet);
            LittleEndianArray view = new LittleEndianArray(packet);
            view.put(3, 1, authMode.ordinal());
            view.put(4,4,serialNumber);
            int flags = 0xff & packet[1];
            if (pairingEnabled) {
                flags |= 0x8;
            }
            packet[1] = (byte) flags;
        }

        public BeaconAuthPayload setAuthMode(AuthMode authMode) {
            this.authMode = authMode;
            return this;
        }

        public BeaconAuthPayload setSerialNumber(int serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        @Override
        public BeaconAuthPayload setDataAvailable(boolean dataAvailable) {
            return (BeaconAuthPayload) super.setDataAvailable(dataAvailable);
        }

        public BeaconAuthPayload setPairingEnabled(boolean pairingEnabled) {
            this.pairingEnabled = pairingEnabled;
            return this;
        }


    }
}
