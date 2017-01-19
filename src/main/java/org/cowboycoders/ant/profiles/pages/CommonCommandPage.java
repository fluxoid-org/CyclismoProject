package org.cowboycoders.ant.profiles.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.common.Defines;

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
        this.lastCommandPage = BitManipulation.UnsignedNumFrom1LeByte(packet[COMMAND_OFFSET]);
        this.lastSequenceNumber = BitManipulation.UnsignedNumFrom1LeByte(packet[SEQUENCE_OFFSET]);
        status = Defines.GenericCommandStatus.getValueFromInt(BitManipulation.UnsignedNumFrom1LeByte(packet[STATUS_OFFSET]));
        responseData = Arrays.copyOfRange(packet, RESPONSE_OFFSET, RESPONSE_END);
    }

    public static byte [] encode(int lastCommandPage, int lastSequenceNumber, Defines.GenericCommandStatus status, final byte [] response) {
        // -1 offset from decode is becuase we do not include the msg id
        final byte [] array = new byte[8];
        array[0] = 71;
        array[1] = (byte)(0xFF & lastCommandPage);
        array[2] = (byte)(0xFF & lastSequenceNumber);
        array[3] = (byte)(0xFF & status.getIntValue());
        System.arraycopy(response, 0, array, 4, RESPONSE_LENGTH);
        return array;
    }
}
