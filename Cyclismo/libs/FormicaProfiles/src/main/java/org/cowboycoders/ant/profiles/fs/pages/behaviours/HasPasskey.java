package org.cowboycoders.ant.profiles.fs.pages.behaviours;

import org.cowboycoders.ant.profiles.fs.defines.AuthResponseCode;
import org.cowboycoders.ant.profiles.fs.pages.responses.AuthResponse;

public interface HasPasskey extends AuthBehaviour {

    int getSerial();
    byte[] getPasskey();

    default AuthResponse onAcceptAuth() {
        return new AuthResponse(AuthResponseCode.ACCEPT, getSerial(), getPasskey());
    }
}
