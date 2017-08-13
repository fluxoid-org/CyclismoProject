package org.cowboycoders.ant.profiles.common.decode.interfaces;


/**
 * Created by fluxoid on 08/02/17.
 */
public interface CalorieCountDecodable {
    boolean isCummulativeCaloriesAvailable();

    Integer getCalorieCounter();

    long getCalorieDelta(CalorieCountDecodable old);
}
