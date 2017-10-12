package org.cowboycoders.ant.profiles.fs.pages.behaviours;

import org.cowboycoders.ant.profiles.fs.defines.AuthResponseCode;
import org.cowboycoders.ant.profiles.fs.pages.AuthMode;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAuth;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.fs.pages.responses.AuthResponse;

public class PassThroughBehaviour implements AuthBehaviour {
    @Override
    public AuthMode getAuthMode() {
        return AuthMode.PASSTHROUGH;
    }

    @Override
    public void onReceieveAuthCmd(AuthCommand cmd, AuthCallback callback) {
        callback.onAccept();
    }

    @Override
    public AuthResponse onAcceptAuth() {
        // may need to use real serial number?
        return new AuthResponse(AuthResponseCode.ACCEPT,0,new byte[0]);
    }
}
