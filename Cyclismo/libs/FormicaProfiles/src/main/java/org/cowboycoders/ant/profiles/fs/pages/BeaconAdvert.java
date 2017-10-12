package org.cowboycoders.ant.profiles.fs.pages;

import org.cowboycoders.ant.profiles.fs.defines.RequestedChannelPeriod;
import org.cowboycoders.ant.utils.IntUtils;
import org.fluxoid.utils.ValidationUtils;
import org.fluxoid.utils.bytes.LittleEndianArray;

public class BeaconAdvert extends CommonBeacon {

    private final int fsDeviceType;
    private final int manufacturerID;
    private final RequestedChannelPeriod requestedChannelPeriod;

    public BeaconAdvert(byte [] data) {
        super(data);
        LittleEndianArray view = new LittleEndianArray(data);
        fsDeviceType = view.unsignedToInt(4,2);
        // 7 bit manufacturer id
        manufacturerID = 0x7FFF & view.unsignedToInt(6,2);
        requestedChannelPeriod = RequestedChannelPeriod.from(data[1]);
        /// 0x8 is possible pairing flag
    }


    public int getFsDeviceType() {
        return fsDeviceType;
    }

    public int getManufacturerID() {
        return manufacturerID;
    }

    public RequestedChannelPeriod getRequestedChannelPeriod() {
        return requestedChannelPeriod;
    }

    public static class BeaconPayload extends CommonBeaconPayload {
        private int fsDeviceType = 0;
        private int manufacturerID = 0;
        private RequestedChannelPeriod requestedChannelPeriod = RequestedChannelPeriod._4096;

        public BeaconPayload() {
            super.setState(State.LINK);
        }

        @Override
        public void encode(byte[] packet) {
            // this clobbers data already in packet
            super.encode(packet);
            LittleEndianArray view = new LittleEndianArray(packet);
            view.put(4,2, fsDeviceType);
            view.put(6,2, manufacturerID);
            packet[1] = requestedChannelPeriod.encode(packet[1]);
        }

        public int getFsDeviceType() {
            return fsDeviceType;
        }

        public BeaconPayload setFsDeviceType(int fsDeviceType) {
            ValidationUtils.validate(0,IntUtils.maxUnsigned(16), fsDeviceType);
            this.fsDeviceType = fsDeviceType;
            return this;
        }

        public int getManufacturerID() {
            return manufacturerID;
        }

        public BeaconPayload setManufacturerID(int manufacturerID) {
            ValidationUtils.validate(0,IntUtils.maxUnsigned(15), manufacturerID);
            this.manufacturerID = manufacturerID;
            return this;
        }

        public RequestedChannelPeriod getRequestedChannelPeriod() {
            return requestedChannelPeriod;
        }

        public BeaconPayload setRequestedChannelPeriod(RequestedChannelPeriod requestedChannelPeriod) {
            this.requestedChannelPeriod = requestedChannelPeriod;
            return this;
        }

        @Override
        public BeaconPayload setDataAvailable(boolean dataAvailable) {
            return (BeaconPayload) super.setDataAvailable(dataAvailable);
        }

    }

}
