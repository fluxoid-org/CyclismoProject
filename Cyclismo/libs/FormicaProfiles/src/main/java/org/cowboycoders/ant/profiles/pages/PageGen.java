package org.cowboycoders.ant.profiles.pages;

import org.fluxoid.utils.bytes.LittleEndianArray;

/**
 * Created by fluxoid on 09/02/17.
 */
public class PageGen {

    public static int DEFAULT_MANUFACTURER_ID = 15;
    public static int DEFAULT_PRODUCT_ID = 65532; // product id of android plugin?

    /**
     * Maybe used for training files
     * @param requestedCommandId
     * @param burstSeqNum
     * @param manufacturerId
     * @param productId (this one was a guess)
     * @param serialNumber
     * @param array
     * @return
     */
    public static byte[] getCommandBurstDataPage(final int requestedCommandId, final int burstSeqNum, final int manufacturerId, final int productId, final long serialNumber, final byte[] array) {
        final int burstLength = 16 + array.length;
        final byte[] burstData = new byte[burstLength];
        burstData[0] = 72;
        burstData[1] = (byte) (requestedCommandId & 0xFF);
        burstData[2] = (byte) (burstSeqNum & 0xFF);
        burstData[3] = (byte) (burstLength / 8); // number of packets
        for (int i = 4; i <= 7; ++i) {
            burstData[i] = -1;
        }
        LittleEndianArray viewer = new LittleEndianArray(burstData);
        viewer.putUnsigned(8,2, manufacturerId);
        viewer.putUnsigned(10,2, productId);
        viewer.put(12,4, serialNumber);
        System.arraycopy(array, 0, burstData, 16, array.length);
        return burstData;
    }


}
