package org.cowboycoders.ant.profiles.fs.pages.behaviours;

import org.cowboycoders.ant.profiles.fs.pages.AuthMode;

public interface PasskeyBehaviour extends AuthBehaviour, HasPasskey {

    @Override
    default AuthMode getAuthMode() {
        return AuthMode.PASSKEY;
    }

}


