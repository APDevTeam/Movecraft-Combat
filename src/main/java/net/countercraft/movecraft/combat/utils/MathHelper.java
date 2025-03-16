package net.countercraft.movecraft.combat.utils;

import org.bukkit.Axis;
import org.bukkit.util.Vector;

public class MathHelper {

    public static double clamp(double value) {
        // Double.MIN_VALUE represents the lowest POSITIVE double value to match IEEE754 format
        return clamp(-Double.MAX_VALUE, Double.MAX_VALUE, value);
    }

    // Same as with doubles!
    public static float clamp(float value) {
        return clamp(-Float.MAX_VALUE, Float.MAX_VALUE, value);
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

    public static void clampVectorModify(final Vector vector) {
        vector.setX(clamp(vector.getX()));
        vector.setY(clamp(vector.getY()));
        vector.setZ(clamp(vector.getZ()));
    }
    public static Vector clampVector(final Vector vector) {
        Vector result = vector.clone();
        clampVectorModify(result);
        return result;
    }

}
