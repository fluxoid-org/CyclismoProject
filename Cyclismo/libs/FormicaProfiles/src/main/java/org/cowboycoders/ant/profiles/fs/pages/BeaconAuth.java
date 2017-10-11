package org.cowboycoders.ant.profiles.fs.pages;

import org.fluxoid.utils.bytes.LittleEndianArray;

public class BeaconAuth extends CommonBeacon {


    private final int serialNumber;
    private final AuthMode authMode;

    public BeaconAuth(byte[] data) {
        super(data);
        LittleEndianArray view = new LittleEndianArray(data);
        serialNumber = view.unsignedToInt(4, 4);
        authMode = AuthMode.decode(data[3]);
    }

    public int getSerialNumber() {
        return serialNumber;
    }


    public static class BeaconAuthPayload extends CommonBeaconPayload  {

        private int serialNumber;
        private AuthMode authMode;


        public BeaconAuthPayload() {
            setState(State.AUTH);
        }

        @Override
        public void encode(byte[] packet) {
            super.encode(packet);
            LittleEndianArray view = new LittleEndianArray(packet);
            view.put(3, 1, authMode.ordinal());
            view.put(4,4,serialNumber);
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


    }
}
