package org.cowboycoders.ant.profiles.common.decode.power;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fluxoid on 04/01/17.
 */
public class PowerOnlyDecoder extends PowerDecodingBehaviour {


    private PowerOnlyDelta delta;

    @Override
    public void onUpdate(AbstractPowerData param) {
        delta = (PowerOnlyDelta) param;

    }

    @Override
    public BigDecimal getPower() {
        return new BigDecimal(delta.getPowerDelta())
                .divide(new BigDecimal(delta.getEventDelta()),
                        5, RoundingMode.HALF_UP);
    }
}
