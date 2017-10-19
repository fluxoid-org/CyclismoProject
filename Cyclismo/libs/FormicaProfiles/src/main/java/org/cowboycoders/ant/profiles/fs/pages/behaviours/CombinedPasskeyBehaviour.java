package org.cowboycoders.ant.profiles.fs.pages.behaviours;

import java9.util.Optional;
import org.cowboycoders.ant.profiles.fs.pages.AuthMode;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAuth;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.fs.pages.responses.AuthResponse;
import org.cowboycoders.ant.profiles.fs.pages.responses.SerialResponse;

public class CombinedPasskeyBehaviour implements AuthBehaviour {

    private final AuthBehaviour pairingBehavior;
    private final AuthBehaviour passkeyBehavior;
    private final AuthInfo authInfo;

    private AuthBehaviour state;

    public CombinedPasskeyBehaviour(PairingBehaviour pairing, PasskeyBehaviour passkey, AuthInfo authInfo) {
        this.pairingBehavior = pairing;
        this.passkeyBehavior = passkey;
        this.authInfo = authInfo;
        state = passkey;
    }


    @Override
    public Optional<AuthResponse> onReceieveAuthCmd(AuthCommand cmd, AuthCallback callback) {
        switch (cmd.getMode()) {
            case PASSKEY:
                state = passkeyBehavior;
                break;
            case SERIAL:
                state = passkeyBehavior;
                return Optional.of(new SerialResponse(authInfo.getSerial()));
            case PAIRING:
                state = pairingBehavior;
        }
        return state.onReceieveAuthCmd(cmd, callback);
    }

    @Override
    public AuthResponse onAcceptAuth() {
        AuthResponse response = state.onAcceptAuth();
        // hack : this is to change to passkey behaviour after pairing is accepted
        state = passkeyBehavior;
        return response;
    }

    @Override
    public boolean isCmdAcceptable(AuthCommand cmd) {
        return state.isCmdAcceptable(cmd);
    }

    @Override
    public AuthMode getAuthMode() {
        return state.getAuthMode();
    }

    @Override
    public BeaconAuth.BeaconAuthPayload onLink(BeaconAuth.BeaconAuthPayload partial) {
        return state.onLink(partial);
    }

}


