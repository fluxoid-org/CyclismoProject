package org.cowboycoders.ant.profiles.fs.pages.behaviours;

import org.cowboycoders.ant.profiles.fs.pages.AuthMode;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAuth;

public interface PairingBehaviour extends HasPasskey {

    @Override
    default BeaconAuth.BeaconAuthPayload onLink(BeaconAuth.BeaconAuthPayload partial) {
        partial.setAuthMode(AuthMode.PAIRING);
        partial.setPairingEnabled(true);
        return partial;
    }

    @Override
    default AuthMode getAuthMode() {
        return AuthMode.PAIRING;
    }

}
