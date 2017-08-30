package org.cowboycoders.ant.profiles.common.decode.interfaces;


import java.math.BigDecimal;

/**
 * Accumulated distance to distance
 */
public interface DistanceDecodable {

    long getDistanceDelta(DistanceDecodable old);
    Integer getDistanceCovered();
    /**
     * The distance can be optional
     * @return false if distance is not available, true otherwise
     */
    boolean isDistanceAvailable();
}
