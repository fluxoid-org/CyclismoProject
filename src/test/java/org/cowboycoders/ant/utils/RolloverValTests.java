package org.cowboycoders.ant.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.org.cowboycoders.ant.profiles.simulators.RollOverVal;

import static org.cowboycoders.ant.profiles.BitManipulation.UNSIGNED_INT8_MAX;
import static org.junit.Assert.assertEquals;

/**
 * Created by fluxoid on 02/02/17.
 */
public class RolloverValTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void underRollover() {
        RollOverVal val = new RollOverVal(UNSIGNED_INT8_MAX);
        final int trial = 200;
        val.setValue(trial);
        assertEquals(trial, val.get());
    }

    @Test
    public void overRollover() {
        RollOverVal val = new RollOverVal(UNSIGNED_INT8_MAX);
        final int trial = UNSIGNED_INT8_MAX + 1;
        val.setValue(trial);
        assertEquals(0, val.get());
    }

    @Test
    public void overRollover2() {
        // 2 rollovers
        RollOverVal val = new RollOverVal(UNSIGNED_INT8_MAX);
        final int trialDelta = 127;
        final int trial = 2 + 2 * UNSIGNED_INT8_MAX + trialDelta;
        val.setValue(trial);
        assertEquals(trialDelta, val.get());
    }

    @Test
    public void overflow() {
        thrown.expect(IllegalStateException.class);
        RollOverVal val = new RollOverVal(UNSIGNED_INT8_MAX);
        val.setValue(Long.MAX_VALUE);
        val.add(1);
        val.get();
    }


}
