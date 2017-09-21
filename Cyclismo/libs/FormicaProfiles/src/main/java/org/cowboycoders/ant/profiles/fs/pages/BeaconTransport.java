package org.cowboycoders.ant.profiles.fs.pages;

public class BeaconTransport extends CommonBeacon {

    public BeaconTransport(byte[] data) {
        super(data);
    }

    public static class BeaconTransportPayload extends CommonBeaconPayload {
        public BeaconTransportPayload() {
            super.setState(State.TRANSPORT);
        }
    }
}
