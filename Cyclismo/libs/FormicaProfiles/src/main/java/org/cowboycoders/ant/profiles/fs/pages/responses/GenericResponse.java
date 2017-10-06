package org.cowboycoders.ant.profiles.fs.pages.responses;

import org.cowboycoders.ant.profiles.fs.defines.ResponseCode;
import org.cowboycoders.ant.profiles.pages.BurstEncodable;
import org.cowboycoders.ant.profiles.pages.SinglePacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.io.ByteArrayOutputStream;

public class GenericResponse implements AntPage, BurstEncodable {

    private final ResponseTo what;
    private final ResponseCode code;

    public GenericResponse(ResponseTo what, ResponseCode code) {
        this.what = what;
        this.code = code;
    }

    public ResponseCode getResponseCode() {
        return code;
    }

    @Override
    public void encode(ByteArrayOutputStream os) {
        os.write(getPageNumber());
        os.write(what.encode());
        os.write(code.encode());
        os.write(0); // padding (but this maybe download specific)
    }

    /**
     *
     * @return how many bytes we write when encoding
     */
    public int getLength() {
        return 4;
    }

    public enum ResponseTo {
        DOWNLOAD(0x89),
        UNKNOWN(0xff);

        private final byte val;
        private static final ResponseTo [] values = ResponseTo.values();

        ResponseTo(int i) {
            this.val = (byte) i;
        }

        public static ResponseTo decode(byte b) {
            for (ResponseTo value: values) {
                if (b == value.val) {
                    return value;
                }
            }
            return UNKNOWN;
        }

        public byte encode() {
            return this.val;
        }
    }

    @Override
    public int getPageNumber() {
        return 68;
    }
}
