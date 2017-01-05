/**
 * Created by fluxoid on 27/12/16.
 */


import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.common.PageDispatcher;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.CommonPageData;

public class Test {

    public static void main(String[] args) throws InterruptedException {
//        PageDispatcher test = new PageDispatcher();
//        test.addListener(CommonPageData.class, new BroadcastListener<CommonPageData>() {
//            @Override
//            public void receiveMessage(CommonPageData commonState) {
//                System.out.println(commonState.isLapFlag());
//                System.out.println("it worked");
//            }
//        });
//        test.dispatch();
//        Thread.sleep(100);
        for (int i =0; i < 10; i++) {

            System.out.println("i");
            for (int j=0; j< 10; j++) {
                System.out.println("j");
                for (int k=0; k<10; k++) {
                    System.out.println("k");
                    break;
                }
            }
        }

    }

}
