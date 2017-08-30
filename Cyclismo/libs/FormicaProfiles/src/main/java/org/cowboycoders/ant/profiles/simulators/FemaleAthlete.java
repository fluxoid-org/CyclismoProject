package org.cowboycoders.ant.profiles.simulators;

/**
 * Created by fluxoid on 08/02/17.
 */
public class FemaleAthlete extends Athlete {
    /*
     * @param height in cm
     * @param weight in kg
     * @param age    in years
     */
    protected FemaleAthlete(double height, double weight, int age) {
        super(Sex.FEMALE, height, weight, age);
    }
}
