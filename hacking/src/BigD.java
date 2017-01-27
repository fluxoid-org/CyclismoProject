import java.math.BigDecimal;

/**
 * Created by fluxoid on 26/01/17.
 */
public class BigD {

    public static void main(String [] args) {
        boolean b = new BigDecimal(1.00).setScale(2).equals(new BigDecimal(2.0).divide(new BigDecimal(2)).setScale(2));
        System.out.println(b);
    }
}
