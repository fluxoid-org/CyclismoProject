package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Capabilities;
import org.cowboycoders.ant.profiles.fitnessequipment.CapabilitiesBuilder;
import org.cowboycoders.ant.profiles.pages.AntPage;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * p 54
 * Created by fluxoid on 16/01/17.
 */
public class CapabilitiesPage implements AntPage {

    public static final int PAGE_NUMBER = 54;

    private static final int MAX_RESISTANCE_OFFSET = 5;
    private static final int FLAGS_OFFSET = 7;
    public static final int RESISTANCE_MASK = 0x1;
    public static final int POWER_MASK = 0x2;
    public static final int SIM_MASK = 0x4;
    private final Capabilities capabilites;

    public Capabilities getCapabilites() {
        return capabilites;
    }

    public static class CapabilitiesPayload {
        private Capabilities capabilites = new CapabilitiesBuilder().createCapabilities();

        public Capabilities getCapabilites() {
            return capabilites;
        }

        public CapabilitiesPayload setCapabilites(Capabilities capabilites) {
            this.capabilites = capabilites;
            return this;
        }

        public void encode(byte[] packet) {
            if (capabilites.getMaximumResistance() != null) {
                PutUnsignedNumIn2LeBytes(packet, MAX_RESISTANCE_OFFSET, capabilites.getMaximumResistance());
            } else {
                PutUnsignedNumIn2LeBytes(packet, MAX_RESISTANCE_OFFSET, UNSIGNED_INT16_MAX);
            }
            byte flags = 0;
            if (capabilites.isBasicResistanceModeSupported()) {
                flags |= RESISTANCE_MASK;
            }
            if (capabilites.isSimulationModeSupported()) {
                flags |= SIM_MASK;
            }
            if (capabilites.isTargetPowerModeSupported()) {
                flags |= POWER_MASK;
            }
            packet[FLAGS_OFFSET] = flags;
        }
    }

    public CapabilitiesPage(byte[] packet) {
        packet[0] = PAGE_NUMBER;
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
