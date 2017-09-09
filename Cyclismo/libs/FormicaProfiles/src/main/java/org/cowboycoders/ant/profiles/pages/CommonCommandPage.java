package org.cowboycoders.ant.profiles.pages;

import org.cowboycoders.ant.profiles.common.Defines;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.util.Arrays;

/**
 * p71
 * Created by fluxoid on 17/01/17.
 */
public class CommonCommandPage implements AntPage {
    private static final int COMMAND_OFFSET = 1;
    private static final int SEQUENCE_OFFSET = 2;
    private static final int STATUS_OFFSET = 3;
    private static final int RESPONSE_OFFSET = 4;
    private static final int RESPONSE_LENGTH = 4;
    private static final int RESPONSE_END = RESPONSE_OFFSET + RESPONSE_LENGTH;
    public static final int PAGE_NUMBER = 71;

    private int lastCommandPage;
    private int lastSequenceNumber;
    private final byte[] responseData;
    private Defines.GenericCommandStatus status;

    public int getLastCommandPage() {
        return lastCommandPage;
    }

    public int getLastSequenceNumber() {
        return lastSequenceNumber;
    }

    public byte[] getResponseData() {
        return responseData;
    }

    public Defines.GenericCommandStatus getStatus() {
        return status;
    }

    public CommonCommandPage(byte [] packet) {
        LittleEndianArray viewer = new LittleEndianArray(packet);
        this.lastCommandPage = viewer.unsignedToInt(COMMAND_OFFSET, 1);
        this.lastSequenceNumber = viewer.unsignedToInt(SEQUENCE_OFFSET, 1);
        status = Defines.GenericCommandStatus.getValueFromInt(viewer.unsignedToInt(STATUS_OFFSET, 1));
        responseData = Arrays.copyOfRange(packet, RESPONSE_OFFSET, RESPONSE_END);
    }

    public static byte [] encode(int lastCommandPage, int lastSequenceNumber, Defines.GenericCommandStatus status, final byte [] response) {
        // -1 offset from decode is becuase we do not include the msg id
        final byte [] array = new byte[8];
        array[0] = PAGE_NUMBER;
        array[1] = (byte)(0xFF & lastCommandPage);
        array[2] = (byte)(0xFF & lastSequenceNumber);
        array[3] = (byte)(0xFF & status.getIntValue());
        System.arraycopy(response, 0, array, 4, RESPONSE_LENGTH);
        return array;
    }

    public static byte [] createEmptyResponse() {
        final byte [] result = new byte[RESPONSE_LENGTH];
        result[PAGE_OFFSET] = PAGE_NUMBER;
        return result;
    }

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    /**
     * Created by fluxoid on 17/01/17.
     */
    public static class CommandStatus {

        private org.cowboycoders.ant.profiles.fitnessequipment.Defines.CommandId lastReceivedCommandId;
        private int lastReceivedSequenceNumber;
        private byte[] rawResponseData;
        private org.cowboycoders.ant.profiles.fitnessequipment.Defines.Status status;

        public org.cowboycoders.ant.profiles.fitnessequipment.Defines.CommandId getLastReceivedCommandId() {
            return lastReceivedCommandId;
        }

        public int getLastReceivedSequenceNumber() {
            return lastReceivedSequenceNumber;
        }

        public byte[] getRawResponseData() {
            return rawResponseData;
        }

        public org.cowboycoders.ant.profiles.fitnessequipment.Defines.Status getStatus() {
            return status;
        }


        protected CommandStatus(org.cowboycoders.ant.profiles.fitnessequipment.Defines.CommandId lastReceivedCommandId, int lastReceivedSequenceNumber, byte[] rawResponseData, org.cowboycoders.ant.profiles.fitnessequipment.Defines.Status status) {
            this.lastReceivedCommandId = lastReceivedCommandId;
            this.lastReceivedSequenceNumber = lastReceivedSequenceNumber;
            this.rawResponseData = rawResponseData;
            this.status = status;
        }

        public void encode(final byte[] array) {
            array[0] = PAGE_NUMBER;
            array[1] = (byte)(0xFF & lastReceivedCommandId.getIntValue());
            array[2] = (byte)(0xFF & lastReceivedSequenceNumber);
            array[3] = (byte)(0xFF & status.getIntValue());
            if (rawResponseData.length != 0) {
                System.arraycopy(rawResponseData, 0, array, 4, RESPONSE_LENGTH);
            }
        }




    }

    public static class CommandStatusBuilder {
        private org.cowboycoders.ant.profiles.fitnessequipment.Defines.CommandId lastReceivedCommandId = org.cowboycoders.ant.profiles.fitnessequipment.Defines.CommandId.UNRECOGNIZED;
        private int lastReceivedSequenceNumber = -1;
        private byte[] rawResponseData = new byte [0];
        private org.cowboycoders.ant.profiles.fitnessequipment.Defines.Status status = org.cowboycoders.ant.profiles.fitnessequipment.Defines.Status.UNINITIALIZED;

        public CommandStatusBuilder setLastReceivedCommandId(org.cowboycoders.ant.profiles.fitnessequipment.Defines.CommandId lastReceivedCommandId) {
            this.lastReceivedCommandId = lastReceivedCommandId;
            return this;
        }

        public CommandStatusBuilder setLastReceivedSequenceNumber(int lastReceivedSequenceNumber) {
            this.lastReceivedSequenceNumber = lastReceivedSequenceNumber;
            return this;
        }

        public CommandStatusBuilder setRawResponseData(byte[] rawResponseData) {
            this.rawResponseData = rawResponseData;
            return this;
        }

        public CommandStatusBuilder setStatus(org.cowboycoders.ant.profiles.fitnessequipment.Defines.Status status) {
            this.status = status;
            return this;
        }

        public static CommandStatusBuilder from(CommandStatus status) {
            return new CommandStatusBuilder()
                    .setLastReceivedCommandId(status.getLastReceivedCommandId())
                    .setLastReceivedSequenceNumber(status.getLastReceivedSequenceNumber())
                    .setStatus(status.getStatus())
                    .setRawResponseData(status.getRawResponseData());
        }

        public CommandStatus createCommandStatus() {
            return new CommandStatus(lastReceivedCommandId, lastReceivedSequenceNumber, rawResponseData, status);
        }
    }
}
