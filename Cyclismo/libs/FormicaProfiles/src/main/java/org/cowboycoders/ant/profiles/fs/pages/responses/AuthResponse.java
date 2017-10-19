package org.cowboycoders.ant.profiles.fs.pages.responses;

import org.cowboycoders.ant.profiles.fs.defines.AuthResponseCode;
import org.fluxoid.utils.Format;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.io.ByteArrayOutputStream;

public class AuthResponse extends GenericResponse {

    private final AuthResponseCode responseCode;

    // may not be needed for all auth types (passthrough?!)
    private final int serialNum;
    private final byte[] passkey;

    public AuthResponse(AuthResponseCode responseCode, int serialNum, byte[] passkey) {
        super(ResponseTo.AUTH);
        this.responseCode = responseCode;
        this.serialNum = serialNum;
        this.passkey = passkey;
    }

    @Override
    public void encode(ByteArrayOutputStream os) {
        super.encode(os);
        byte code = responseCode.encode();
        os.write(code);
        os.write(passkey.length);
        byte[] serialNumBytes = new byte[4];
        LittleEndianArray view = new LittleEndianArray(serialNumBytes);
        view.putUnsigned(0, 4, serialNum);
        os.write(serialNumBytes,0, serialNumBytes.length);
        os.write(passkey, 0, passkey.length);
        byte [] padding = new byte[getPaddingLen()];
        if (padding.length > 0) {
            os.write(padding,0, padding.length);
        }
    }

    private int getPaddingLen() {
        return (8 - (passkey.length) % 8) % 8;
    }
}
