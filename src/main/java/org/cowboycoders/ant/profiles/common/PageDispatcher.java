package org.cowboycoders.ant.profiles.common;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.*;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
        final byte page = data[AntPage.PAGE_OFFSET];
        switch (page) {
            case 1:
                return new CalibrationResponse(data);
            case 2:
                return new CalibrationProgress(data);
            case 16:
                return new GeneralData(data);
            case 17:
                return new GeneralSettings(data);
            case 18:
                return new MetabolicData(data);
            case 21:
                return new BikeData(data);
            case 25:
                return new TrainerData(data);
            case 26:
                return new TorqueData(data);
            case 48:
                return new PercentageResistance(data);
            case 49:
                return new TargetPower(data);
            case 50:
                return new WindResistance(data);
            case 51:
                return new TrackResistance(data);
            case 54:
                return new CapabilitiesPage(data);
            case 55:
                return new ConfigPage(data);
            case 71:
                return new Command(data);
        }
        return null;
    }

    public void dispatch(final byte[] data) {
        AntPage page = decode(data);
        if (page != null) {
            bus.sendMessage(page);
        } else {
            Logger.getGlobal().warning("no handler for: " + data);
        }
    }
}
