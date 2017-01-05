package org.cowboycoders.ant.profiles.common;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.CommonPageData;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fluxoid on 30/12/16.
 */
public class PageDispatcher {

    private BroadcastMessenger<AntPage> bus = new BroadcastMessenger();
    private Map<Object, BroadcastListener<AntPage>> adapterListenerMap = new HashMap();


    public <A extends AntPage> void addListener(Class<A> clazz, BroadcastListener<A> listener) {
        //TODO: factor out this pattern as it is used in jformica.Node
        BroadcastListener<AntPage> wrapper = o -> {
            if (clazz.isInstance(o)) {
                listener.receiveMessage(clazz.cast(o));
            }
        };
        adapterListenerMap.put(listener, wrapper);
        bus.addBroadcastListener(wrapper);
    }

    /**
     *
     * @param listener recieves updates for <A><
     * @param <A> interesting object
     * @return true on success
     */
    public <A extends AntPage> boolean removeListener(BroadcastListener<A> listener) {
        BroadcastListener<AntPage> wrapper = adapterListenerMap.get(listener);
        if (wrapper == null) {return false;}
        bus.removeBroadcastListener(wrapper);
        return true;
    }

    public AntPage decode(byte[] data) {
        final byte page = data[0];
        switch (page) {
            case 1:
                //return new CommonPageData(data);
        }
        return null;
    }

    public void dispatch() {
        bus.sendMessage(decode(new byte[] {1,0,0,0,0,0,0,0,(byte) (0 | 0x80)}));
    }
}
