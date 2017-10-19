package org.cowboycoders.ant.profiles.fs.pages.behaviours;

import java9.util.Optional;
import org.cowboycoders.ant.profiles.fs.pages.AuthMode;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAuth;
import org.cowboycoders.ant.profiles.fs.pages.cmd.AuthCommand;
import org.cowboycoders.ant.profiles.fs.pages.responses.AuthResponse;

public interface AuthBehaviour {

    /**
     *
     * @return AuthMode we advertise as
     */
    AuthMode getAuthMode();

    /**
     * Hook to change auth beacon
     * @param partial partially completed payload that can be modified
     * @return a new payload
     */
    default BeaconAuth.BeaconAuthPayload onLink(BeaconAuth.BeaconAuthPayload partial) {
        partial.setAuthMode(getAuthMode());
        return partial;
    }

    /**
     *
     * @param cmd to be inspected
     * @return true if we can handle cmd
     */
    default boolean isCmdAcceptable(AuthCommand cmd) {
        return cmd.getMode() == getAuthMode();
    }

    /**
     *
     * @param cmd the command received from client
     * @param callback for delayed responses (i.e pairing needs user input)
     * @return an optional response that is sent without without making state transition
     */
    Optional<AuthResponse> onReceieveAuthCmd(AuthCommand cmd, AuthCallback callback);

    /**
     *
     * @return response we want to send when {@see AuthCallback#onAccept} is called
     */
    AuthResponse onAcceptAuth();


}
