package org.cowboycoders.ant.profiles.fs.pages.responses;

import org.cowboycoders.ant.profiles.fs.defines.ResponseCode;
import org.cowboycoders.ant.profiles.fs.pages.BeaconTransport;
import org.fluxoid.utils.Format;
import org.fluxoid.utils.bytes.LittleEndianArray;
import org.fluxoid.utils.crc.Crc16Utils;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class DownloadResponse extends GenericResponse {

    private static final int CRC_LEN = 2;
    public static final int EXTRA_LEN = 12;
    public static final int BEACON_TRANSPORT_LEN = 8;
    private final int offset;
    private final int initialCrc;
    private final byte[] file;
    private final byte[] payload;
    private final int crc;

    public DownloadResponse(ResponseCode code, byte [] file, int chunkSize, int offset, int initalCrc) {
        super(ResponseTo.DOWNLOAD, code);
        int end = Math.min(offset + chunkSize, file.length);
        payload = Arrays.copyOfRange(file, offset, end);
        this.offset = offset;
        this.file = file;
        this.initialCrc = initalCrc;
        this.crc = Crc16Utils.computeCrc(Crc16Utils.ANSI_CRC16_TABLE, initialCrc, payload);
    }

    public int getLength() {
        return super.getLength() + getPaddingLen() + EXTRA_LEN + BEACON_TRANSPORT_LEN + CRC_LEN + payload.length;
    }

    public int getPayloadCrc() {
        return crc;
    }

    private void encodeNoError(ByteArrayOutputStream os) {
        byte [] extra = new byte[EXTRA_LEN];
        LittleEndianArray view = new LittleEndianArray(extra);
        view.putUnsigned(0,4, payload.length);
        view.putUnsigned(4,4, offset);
        view.putUnsigned(8,4, file.length);
        os.write(extra,0, extra.length);

        os.write(payload, 0, payload.length);

        byte [] paddingAndCrc = new byte[getPaddingLen() + CRC_LEN];
        int crc = getPayloadCrc();
        view = new LittleEndianArray(paddingAndCrc);
        view.putUnsigned(paddingAndCrc.length -2, 2, crc);
        os.write(paddingAndCrc,0, paddingAndCrc.length);
    }

    private void encodeError(ByteArrayOutputStream os) {
        byte [] extra = new byte[EXTRA_LEN];
        LittleEndianArray view = new LittleEndianArray(extra);
        view.putUnsigned(0,4, 0);
        view.putUnsigned(4,4, 0);
        view.putUnsigned(8,4, 0);
        os.write(extra,0, extra.length);
    }

    @Override
    public void encode(ByteArrayOutputStream os) {
        new BeaconTransport.BeaconTransportPayload().encode(os);

        super.encode(os);

        switch (getResponseCode()) {
            case NO_ERROR:
                encodeNoError(os);
                break;
            default:
                encodeError(os);
        }


    }

    private int getPaddingLen() {
        return (8 - (payload.length + CRC_LEN) % 8) % 8;
    }

}
