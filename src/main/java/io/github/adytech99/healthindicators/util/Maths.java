package io.github.adytech99.healthindicators.util;

public class Maths {
    public static double truncate(double number, int places) {
        return Math.floor(number * Math.pow(10, places)) / Math.pow(10, places);
    }
}
