/**
 * Created by fluxoid on 09/01/17.
 */
public class MultiLoop {

    public static void main(String[] args) {
        while (true) {
            System.out.println("outer");
            while (true) {
                System.out.println("inner");
                break;
            }
            System.out.println("outer end");
            break;
        }

        OUTER:
        while (true) {
            System.out.println("outer");
            INNER:
            while (true) {
                System.out.println("inner");
                continue OUTER;
            }
            // unreachable
        }
    }
}
