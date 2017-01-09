import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.responses.Capability;

/**
 * Created by fluxoid on 05/01/17.
 */
public class AntBasicTest {
    public static void main(String[] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);

        node.start();

        for (Capability c : node.getCapabiltites()) {
            System.out.println(c);
        }

        node.stop();


    }
}
