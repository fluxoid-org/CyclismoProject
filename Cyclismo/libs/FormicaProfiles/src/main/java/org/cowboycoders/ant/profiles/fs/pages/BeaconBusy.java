package org.cowboycoders.ant.profiles.fs.pages;

public class BeaconBusy extends CommonBeacon {

    public BeaconBusy(byte[] data) {
        super(data);
    }

    public static class BeaconTransportPayload extends CommonBeaconPayload {
        public BeaconTransportPayload() {
            super.setState(State.BUSY);
        }
    }
}
