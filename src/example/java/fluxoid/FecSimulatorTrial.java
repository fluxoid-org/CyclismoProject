package fluxoid;

import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.profiles.simulators.DummyFecTurbo;

public class FecSimulatorTrial {
    public static void main(String [] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        node.start();
        node.reset();
        new DummyFecTurbo().start(node);
    }
}
