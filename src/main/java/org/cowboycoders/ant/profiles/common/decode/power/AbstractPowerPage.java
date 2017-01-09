package org.cowboycoders.ant.profiles.common.decode.power;

import org.cowboycoders.ant.profiles.common.TelemetryPage;
import org.cowboycoders.ant.profiles.common.decode.CounterBasedPage;

/**
 * Created by fluxoid on 04/01/17.
 */
public interface AbstractPowerPage extends CounterBasedPage {


    boolean isValidDelta(PowerOnlyPage old);
}
