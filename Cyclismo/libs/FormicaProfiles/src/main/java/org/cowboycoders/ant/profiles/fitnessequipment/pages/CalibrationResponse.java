package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.pages.SinglePacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Page 1
 * Created by fluxoid on 02/01/17.
 */
public class CalibrationResponse implements AntPage {

    public static final int PAGE_NUMBER = 1;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    private static final int OFFSET_FLAG__MASK = 0x40;
    private static final int SPINDOWN_FLAG_MASK = 0x80;
    private static final int SPINDOWN_FLAG_OFFSET = 1;
    private static final int SPINDOWN_OFFSET = 6;
    private static final int OFFSET_OFFSET = 4;
    private static final int OFFSET_FLAG_OFFSET = 1;
    private static final int TEMP_OFFSET = 3;


    public boolean isZeroOffsetSuccess() {
        return zeroOffsetSuccess;
    }

    public boolean isSpinDownSuccess() {
        return spinDownSuccess;
    }

    private final boolean zeroOffsetSuccess;
    private final boolean spinDownSuccess;


    /**
     * Some sort of offset in range 0 - 65534 (no units), resolution 1
     */
    public Integer getZeroOffset() {
        return zeroOffset;
    }

    /**
     *  spin down time of wheel/roller in range 0ms - 65534ms, resolution 1ms
     */
    public Integer getSpinDownTime() {
        return spinDownTime;
    }

    /**
     * internal temperature of the trainer: -25C - +100C, resolution: 0.5C
     *
     * @return internal temp, or null if not set
     */
    public BigDecimal getTemp() {
        return temp;
    }

    private final Integer zeroOffset;
    private final Integer spinDownTime;
    private final BigDecimal temp;

    public static class CalibrationResponsePayload implements SinglePacketEncodable {
        private Integer zeroOffset = null;
        private Integer spinDownTime = null;
        private BigDecimal temp = null;
        private boolean zeroOffsetSuccess = false;
        private boolean spinDownSuccess = false;

        public Integer getZeroOffset() {
            return zeroOffset;
        }

        public CalibrationResponsePayload setZeroOffset(Integer zeroOffset) {
            this.zeroOffset = zeroOffset;
            return this;
        }

        public Integer getSpinDownTime() {
            return spinDownTime;
        }

        public CalibrationResponsePayload setSpinDownTime(Integer spinDownTime) {
            this.spinDownTime = spinDownTime;
            return this;
        }

        public BigDecimal getTemp() {
            return temp;
        }

        public CalibrationResponsePayload setTemp(BigDecimal temp) {
            this.temp = temp;
            return this;
        }

        public boolean isZeroOffsetSuccess() {
            return zeroOffsetSuccess;
        }

        public CalibrationResponsePayload setZeroOffsetSuccess(boolean zeroOffsetSuccess) {
            this.zeroOffsetSuccess = zeroOffsetSuccess;
            return this;
        }

        public boolean isSpinDownSuccess() {
            return spinDownSuccess;
        }

        public CalibrationResponsePayload setSpinDownSuccess(boolean spinDownSuccess) {
            this.spinDownSuccess = spinDownSuccess;
            return this;
        }

        public void encode(final byte[] packet) {
            packet[0] = PAGE_NUMBER;
            LittleEndianArray viewer = new LittleEndianArray(packet);
            if (zeroOffsetSuccess) {
                packet[OFFSET_FLAG_OFFSET] |= OFFSET_FLAG__MASK;
            } else {
                packet[OFFSET_FLAG_OFFSET] = clearMaskedBits(packet[OFFSET_FLAG_OFFSET], OFFSET_FLAG__MASK);
            }
            if (spinDownSuccess) {
                packet[SPINDOWN_FLAG_OFFSET] |= SPINDOWN_FLAG_MASK;
            } else {
                packet[SPINDOWN_FLAG_OFFSET] = clearMaskedBits(packet[SPINDOWN_FLAG_OFFSET],SPINDOWN_FLAG_MASK);
            }
            if (temp == null) {
                packet[TEMP_OFFSET] = (byte) (0xff & UNSIGNED_INT8_MAX);
            } else {
                BigDecimal n = temp.add(new BigDecimal(25))
                        .multiply(new BigDecimal(2))
                        .setScale(0, RoundingMode.HALF_UP);
                packet[TEMP_OFFSET] = n.byteValue();
            }
            if (zeroOffset != null) {
                viewer.putUnsigned(OFFSET_OFFSET,2, zeroOffset);
            } else {
                viewer.putUnsigned(OFFSET_OFFSET,2, UNSIGNED_INT16_MAX);
            }
            if (spinDownTime != null) {
                viewer.putUnsigned(SPINDOWN_OFFSET,2, spinDownTime);
            } else {
                viewer.putUnsigned(SPINDOWN_OFFSET,2, UNSIGNED_INT16_MAX);

            }
        }
    }


    public CalibrationResponse(byte[] packet) {
        LittleEndianArray viewer = new LittleEndianArray(packet);
        zeroOffsetSuccess = intToBoolean (packet[OFFSET_FLAG_OFFSET] & OFFSET_FLAG__MASK);
        spinDownSuccess = intToBoolean (packet[SPINDOWN_FLAG_OFFSET] & SPINDOWN_FLAG_MASK);
        final int tempRaw = viewer.unsignedToInt(TEMP_OFFSET,1);
        if (tempRaw != UNSIGNED_INT8_MAX) { // NULL
            temp = new BigDecimal(tempRaw).divide(new BigDecimal(2), 1, BigDecimal.ROUND_HALF_UP).subtract(new BigDecimal(25));
        } else {
            temp = null;
        };
        final int offsetRaw = viewer.unsignedToInt(OFFSET_OFFSET, 2);
        if (offsetRaw != UNSIGNED_INT16_MAX) {
            zeroOffset = offsetRaw;
        } else {
            zeroOffset = null;
        }
        final int spinDownRaw = viewer.unsignedToInt(SPINDOWN_OFFSET,2);
        if (spinDownRaw != UNSIGNED_INT16_MAX) {
            spinDownTime = spinDownRaw;
        } else {
            spinDownTime = null;
        }

    }


}
