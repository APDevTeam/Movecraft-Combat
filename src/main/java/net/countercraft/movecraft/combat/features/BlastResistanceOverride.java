package net.countercraft.movecraft.combat.features;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.util.Tags;

public class BlastResistanceOverride {
    public static Map<Material, Float> BlastResistanceOverride = null;

    public static void load(@NotNull FileConfiguration config) {
        if (!config.contains("BlastResistanceOverride"))
            return;
        var section = config.getConfigurationSection("BlastResistanceOverride");
        if (section == null)
            return;

        BlastResistanceOverride = new HashMap<>();
        for (var entry : section.getValues(false).entrySet()) {
            EnumSet<Material> materials = Tags.parseMaterials(entry.getKey());
            for (Material m : materials) {
                BlastResistanceOverride.put(m, (Float) entry.getValue());
            }
        }
    }

    private static Field getField() {
        return FieldUtils.getField(Material.class, "durability");
    }

    public static boolean setBlastResistance(Material m, float resistance) {
        try {
            FieldUtils.writeField(getField(), m, resistance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean revertToVanilla(Material m) {
        return setBlastResistance(m, m.getBlastResistance()); // Spigot stores the vanilla values for us
    }

    public static void enable() {
        for (var entry : BlastResistanceOverride.entrySet()) {
            setBlastResistance(entry.getKey(), entry.getValue());
        }
    }

    public static void disable() {
        for (Material m : BlastResistanceOverride.keySet()) {
            revertToVanilla(m);
        }
    }
}
