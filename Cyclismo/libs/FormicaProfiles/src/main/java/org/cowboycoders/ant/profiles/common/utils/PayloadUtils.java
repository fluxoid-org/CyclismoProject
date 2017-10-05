package org.cowboycoders.ant.profiles.common.utils;

import org.cowboycoders.ant.messages.data.BroadcastDataMessage;
import org.cowboycoders.ant.profiles.pages.SinglePacketEncodable;

public class PayloadUtils {
    public static BroadcastDataMessage getBroadcastDataMessage(SinglePacketEncodable encodable) {
        byte [] data = new byte[8];
        encodable.encode(data);
        BroadcastDataMessage payload = new BroadcastDataMessage();
        payload.setData(data);
        return payload;
    }
}
