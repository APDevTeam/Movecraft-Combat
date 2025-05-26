package net.countercraft.movecraft.combat.utils;

import org.bukkit.Axis;
import org.bukkit.util.Vector;

public class MathHelper {
    public static double clamp(double value) {
        return clamp(-Double.MAX_VALUE, Double.MAX_VALUE, value);
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

    public static void clampVectorModify(final Vector vector) {
        vector.setX(clamp(vector.getX()));
        vector.setY(clamp(vector.getY()));
        vector.setZ(clamp(vector.getZ()));
    }
}
