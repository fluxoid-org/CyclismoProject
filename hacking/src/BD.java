import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 30/01/17.
 */
public class BD {
    public static void main(String[] args) {
        System.out.println(new BigDecimal(0.004).setScale(3, RoundingMode.HALF_UP));
    }
}
