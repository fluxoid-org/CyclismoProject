import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.events.BroadcastMessenger;
import org.cowboycoders.ant.events.MessageDispatcher;

/**
 * Created by fluxoid on 05/01/17.
 */
public class MessengerTest {
    public static void main(String[] args) {
        BroadcastMessenger<String> test = new BroadcastMessenger<String>();
        test.addBroadcastListener(new BroadcastListener<String>() {

            private boolean sent = false;
            @Override
            public void receiveMessage(String s) {
                if (!sent) {
                    sent = true;
                    test.sendMessage(s + " world!");
                }
                System.out.println(s);
            }
        });
        test.sendMessage("hello");
    }
}
