package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Capabilities;
import org.cowboycoders.ant.profiles.fitnessequipment.CapabilitiesBuilder;
import org.cowboycoders.ant.profiles.pages.AntPage;

import static org.cowboycoders.ant.profiles.BitManipulation.UNSIGNED_INT16_MAX;
import static org.cowboycoders.ant.profiles.BitManipulation.UnsignedNumFrom1LeByte;
import static org.cowboycoders.ant.profiles.BitManipulation.UnsignedNumFrom2LeBytes;

/**
 * p 54
 * Created by fluxoid on 16/01/17.
 */
public class CapabilitiesPage implements AntPage {

    private static final int MAX_RESISTANCE_OFFSET = 6;
    private static final int FLAGS_OFFSET = 8;
    public static final int RESISTANCE_MASK = 0x1;
    public static final int POWER_MASK = 0x2;
    public static final int SIM_MASK = 0x4;
    private final Capabilities capabilites;

    public Capabilities getCapabilites() {
        return capabilites;
    }

    public CapabilitiesPage(byte[] packet) {
        CapabilitiesBuilder builder = new CapabilitiesBuilder();
        int maxResitanceRaw = UnsignedNumFrom2LeBytes(packet, MAX_RESISTANCE_OFFSET);
        if (maxResitanceRaw != UNSIGNED_INT16_MAX) {
            builder.setMaximumResistance(maxResitanceRaw);
        }
        int flags = UnsignedNumFrom1LeByte(packet[FLAGS_OFFSET]);
        builder.setBasicResistanceModeSupport((flags & RESISTANCE_MASK) != 0);
        builder.setTargetPowerModeSupport((flags & POWER_MASK) !=0);
        builder.setSimulationModeSupport((flags & SIM_MASK) != 0);

        capabilites = builder.createCapabilities();


    }

}
