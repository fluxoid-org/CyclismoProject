package fluxoid;

import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.profiles.fitnessequipment.Capabilities;
import org.cowboycoders.ant.profiles.fitnessequipment.Config;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.CalibrationProgress;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.CalibrationResponse;

import java.util.EnumSet;

public class FecProfileTrial {

    public static void main(String [] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        node.start();
        node.reset();
        new org.cowboycoders.ant.profiles.FecProfile() {
            @Override
            public void onEquipmentStateChange(Defines.EquipmentState oldState, Defines.EquipmentState newState) {

            }

            @Override
            public void onCapabilitiesReceived(Capabilities capabilitiesPage) {

            }

            @Override
            public void onConfigRecieved(Config conf) {

            }

            @Override
            public void onCalibrationUpdate(CalibrationProgress progress) {

            }

            @Override
            public void onCalibrationStatusReceieved(CalibrationResponse calibrationResponse) {

            }

            @Override
            public void onConnect() {

            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onStatusChange(EnumSet<Defines.TrainerStatusFlag> oldStatus, EnumSet<Defines.TrainerStatusFlag> newStatus) {

            }

        }.start(node);
    }
}
