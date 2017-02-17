package org.cowboycoders.ant.profiles.simulators;

import org.cowboycoders.ant.Node;

/**
 * Created by fluxoid on 16/02/17.
 */
public interface TurboControllable {
    void start(Node transceiver);
    void setPower(int power);
    void incrementLaps();
    TurboStateViewable getState();
}
