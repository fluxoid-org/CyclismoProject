package org.cowboycoders.ant.profiles.fs.pages.responses;

import org.cowboycoders.ant.profiles.fs.defines.AuthResponseCode;

public class SerialResponse extends AuthResponse {
    public SerialResponse(int serialNum) {
        super(AuthResponseCode.NOT_APPLICABLE, serialNum, new byte [0]);
    }
}
