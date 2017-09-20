package org.fluxoid.utils;

import org.cowboycoders.ant.utils.IntUtils;
import org.junit.Test;

import static org.fluxoid.utils.ValidationUtils.validate;
import static org.fluxoid.utils.ValidationUtils.validateUnsigned;

public class ValidationUtilsTest {

    @Test
    public void onLimits() {
        validate(0,10,10);
        validate(0,10,0);
    }

    @Test( expected = IllegalArgumentException.class)
    public void overLimit() {
        validate(0,10,11);
    }

    @Test
    public void unsignedInt() {
        validateUnsigned(IntUtils.maxUnsigned(32), 0xffffffff);
    }

    @Test
    public void unsignedIntZero() {
        validateUnsigned(IntUtils.maxUnsigned(32), 0);
    }

    @Test( expected = IllegalArgumentException.class)
    public void unsignedOver() {
        int max = IntUtils.maxUnsigned(31);
        validateUnsigned(max+ 1 , max + 2);
    }

}
