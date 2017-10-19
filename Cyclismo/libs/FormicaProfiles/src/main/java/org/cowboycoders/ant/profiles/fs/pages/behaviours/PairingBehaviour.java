package org.cowboycoders.ant.profiles.fs.pages.behaviours;

import java9.util.Optional;
import org.cowboycoders.ant.profiles.fs.defines.AuthResponseCode;
import org.cowboycoders.ant.profiles.fs.pages.AuthMode;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAuth;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.fs.pages.responses.AuthResponse;

public abstract class PairingBehaviour implements AuthBehaviour {

    private final AuthInfo authInfo;

    public PairingBehaviour(AuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    @Override
    public BeaconAuth.BeaconAuthPayload onLink(BeaconAuth.BeaconAuthPayload partial) {
        partial.setAuthMode(AuthMode.PAIRING);
        partial.setPairingEnabled(true);
        return partial;
    }

    @Override
    public AuthResponse onAcceptAuth() {
        return new AuthResponse(AuthResponseCode.ACCEPT, authInfo.getSerial(), authInfo.getPasskey());
    }

    @Override
    public AuthMode getAuthMode() {
        return AuthMode.PAIRING;
    }


}
