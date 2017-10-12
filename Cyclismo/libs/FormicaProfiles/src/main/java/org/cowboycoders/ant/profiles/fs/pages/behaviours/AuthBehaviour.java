package org.cowboycoders.ant.profiles.fs.pages.behaviours;

import org.cowboycoders.ant.profiles.fs.pages.AuthMode;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAuth;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.fs.pages.responses.AuthResponse;

public interface AuthBehaviour {

    public AuthMode getAuthMode();

    default BeaconAuth.BeaconAuthPayload onLink(BeaconAuth.BeaconAuthPayload partial) {
        partial.setAuthMode(getAuthMode());
        return partial;
    }

    public void onReceieveAuthCmd(AuthCommand cmd, AuthCallback callback);
    public AuthResponse onAcceptAuth();

}
