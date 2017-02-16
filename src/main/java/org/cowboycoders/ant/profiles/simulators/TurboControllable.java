package org.cowboycoders.ant.profiles.simulators;

/**
 * Created by fluxoid on 16/02/17.
 */
public interface TurboControllable {
    void setPower(int power);
    void incrementLaps();
    TurboStateViewable getState();
}
