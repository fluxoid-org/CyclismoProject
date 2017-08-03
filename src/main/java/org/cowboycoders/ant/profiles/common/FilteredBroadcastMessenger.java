package org.cowboycoders.ant.profiles.common;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.events.BroadcastMessenger;

import java.util.HashMap;
import java.util.Map;

public class FilteredBroadcastMessenger<T> {
    private BroadcastMessenger<T> bus = new BroadcastMessenger<>();
    private Map<Object, BroadcastListener<T>> adapterListenerMap = new HashMap<>();

    public <A extends T> void addListener(final Class<A> clazz, final BroadcastListener<A> listener) {
        //TODO: factor out this pattern as it is used in jformica.Node
        BroadcastListener<T> wrapper = new BroadcastListener<T>() {
            @Override
            public void receiveMessage(T o) {
                if (clazz.isInstance(o)) {
                    listener.receiveMessage(clazz.cast(o));
                }
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
    public <A extends T> boolean removeListener(BroadcastListener<A> listener) {
        BroadcastListener<T> wrapper = adapterListenerMap.get(listener);
        if (wrapper == null) {return false;}
        bus.removeBroadcastListener(wrapper);
        return true;
    }

    public<A extends T> void send(A msg) {
        bus.sendMessage(msg);
    }

}
