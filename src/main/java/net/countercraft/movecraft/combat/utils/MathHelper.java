package net.countercraft.movecraft.combat.utils;

public class MathHelper {

    public static double clamp(double value) {
        return clamp(Double.MIN_VALUE, Double.MAX_VALUE, value);
    }

    public static float clamp(float value) {
        return clamp(Float.MIN_VALUE, Float.MAX_VALUE, value);
    }

    public static int clamp(int value) {
        return clamp(Integer.MIN_VALUE, Integer.MAX_VALUE, value);
    }

    public static double clamp(double min, double max, double value) {
        if (value > max) {
            return max;
        }
        if (value < min) {
            return min;
        }
        return value;
    }

    public static int clamp(int min, int max, int value) {
        if (value > max) {
            return max;
        }
        if (value < min) {
            return min;
        }
        return value;
    }

    public static float clamp(float min, float max, float value) {
        if (value > max) {
            return max;
        }
        if (value < min) {
            return min;
        }
        return value;
    }

}
