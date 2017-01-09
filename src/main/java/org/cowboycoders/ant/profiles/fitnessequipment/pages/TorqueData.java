package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.pages.AntPage;

import static org.cowboycoders.ant.profiles.BitManipulation.UnsignedNumFrom1LeByte;
import static org.cowboycoders.ant.profiles.BitManipulation.UnsignedNumFrom2LeBytes;

/**
 * Created by fluxoid on 09/01/17.
 */
public class TorqueData extends CommonPageData implements AntPage {

    private static final int EVENT_OFFSET = 2;
    private static final int ROTATION_OFFSET = 3;
    private static final int PERIOD_OFFSET = 4;
    private static final int TORQUE_OFFSET = 6;
    private static final int SOURCE_OFFSET = 1;

    private final int events;
    private final long torqueSum;

    // wheel related
    private final long rotations;
    private final long period;
    private final Defines.TorqueSource source;


    public TorqueData(byte[] packet) {
        super(packet);
        events = UnsignedNumFrom1LeByte(packet[EVENT_OFFSET]);
        rotations = UnsignedNumFrom1LeByte(packet[ROTATION_OFFSET]);
        period = UnsignedNumFrom2LeBytes(packet, PERIOD_OFFSET);
        torqueSum = UnsignedNumFrom2LeBytes(packet, TORQUE_OFFSET);
        source = Defines.TorqueSource.getValueFromInt(UnsignedNumFrom1LeByte(packet[SOURCE_OFFSET]));
    }

}
