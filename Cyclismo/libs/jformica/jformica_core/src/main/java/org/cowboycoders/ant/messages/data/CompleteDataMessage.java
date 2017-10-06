package org.cowboycoders.ant.messages.data;


import org.cowboycoders.ant.messages.Message;
import org.cowboycoders.ant.messages.MessageId;

/**
 * Represents a non partial communication i.e not a burst packet
 */
public class CompleteDataMessage extends DataMessage {

    protected CompleteDataMessage(Message backend, MessageId id, Integer channelNo) {
        super(backend, id, channelNo);
    }
}
