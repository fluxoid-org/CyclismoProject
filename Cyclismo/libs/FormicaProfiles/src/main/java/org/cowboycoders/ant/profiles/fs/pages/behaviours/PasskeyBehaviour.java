package org.cowboycoders.ant.profiles.fs.pages.behaviours;

import java9.util.Optional;
import org.cowboycoders.ant.profiles.fs.defines.AuthResponseCode;
import org.cowboycoders.ant.profiles.fs.pages.AuthMode;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.fs.pages.responses.AuthResponse;
import org.fluxoid.utils.Format;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.logging.Logger;

public class PasskeyBehaviour implements AuthBehaviour {

    private static Logger LOGGER = Logger.getLogger(PasskeyBehaviour.class.getName());

    private final AuthInfo authInfo;
    private EnumSet<AuthMode> supportedModes = EnumSet.of(AuthMode.SERIAL, AuthMode.PAIRING, AuthMode.PASSKEY);

    public PasskeyBehaviour(AuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    @Override
    public boolean isCmdAcceptable(AuthCommand cmd) {
        return supportedModes.contains(cmd.getMode());
    }

    public AuthResponse onAcceptAuth() {
        AuthInfo info = getAuthInfo();
        return new AuthResponse(AuthResponseCode.ACCEPT, info.getSerial(), new byte [0]);
    }

    @Override
    public AuthMode getAuthMode() {
        return AuthMode.PASSKEY;
    }

    @Override
    public Optional<AuthResponse> onReceieveAuthCmd(AuthCommand cmd, AuthCallback callback) {
        if (cmd.getMode().equals(AuthMode.PASSKEY)) {
            if (Arrays.equals(cmd.getPasskey(), authInfo.getPasskey())) {
                LOGGER.finer("accepting passkey: " + Format.bytesToString(cmd.getPasskey()));
                callback.onAccept();
            } else {
                callback.onReject();
                LOGGER.warning("rejecting passkey: " + Format.bytesToString(cmd.getPasskey()));
            }
        }
        return Optional.empty();
    }


    public AuthInfo getAuthInfo() {
        return authInfo;
    }

}
