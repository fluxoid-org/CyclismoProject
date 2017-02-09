package org.org.cowboycoders.ant.profiles.simulators;

import java.math.BigDecimal;

import static org.org.cowboycoders.ant.profiles.simulators.Athlete.Sex.MALE;

/**
 * Created by fluxoid on 08/02/17.
 */

public abstract class Athlete {

    private final Sex sex;
    private final double height;
    private final double weight;
    private final int age;

    public enum Sex {
        MALE,
        FEMALE
    }

    /**
     *
     * @param sex
     * @param weight in kg
     * @param height in cm
     * @param age in years
     */
    protected Athlete(Sex sex, double height, double weight, int age) {
        this.sex = sex;
        this.weight = weight;
        this.height = height;
        this.age = age;
    }


    public Sex getSex() {
        return sex;
    }



    /**
     * Estimation of basal metabolic rate
     * @return kcal/day
     */
    public BigDecimal getBrm() {
        //Mifflin St Jeor, see: https://en.wikipedia.org/wiki/Basal_metabolic_rate
        return new BigDecimal(10 * weight
                + 6.25 * height
                - 5 * age
                + (sex == MALE ? 5 : -161));

    }

    public BigDecimal getBrmPerHour() {
        return getBrm().divide(new BigDecimal(24), 4, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getEstimatedCalorificBurn(double met) {
        // TODO: add activity -> MET map?
        return new BigDecimal(met).multiply(getBrmPerHour());
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public int getAge() {
        return age;
    }
}
