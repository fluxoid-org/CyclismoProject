package org.cowboycoders.ant.profiles.common.decode.power;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fluxoid on 04/01/17.
 */
public class PowerDecoder {
    private final PowerDecodingBehaviour decoder;

    public boolean add(PowerListener powerListener) {
        return listeners.add(powerListener);
    }

    public boolean remove(PowerListener o) {
        return listeners.remove(o);
    }

    private List<PowerListener> listeners = new ArrayList();

    PowerDecoder(PowerDecodingBehaviour behaviour) {
        this.decoder = behaviour;
    }

    public void update(AbstractPowerData o) {
        decoder.update(o);
        if (decoder.isCoasting()) {
            for (PowerListener l : listeners) {
                l.onCoastDetected();
            }
        } else {
            for (PowerListener l : listeners) {
                l.onPowerUpdate(decoder.getPower());
            }
        }
    }


}
