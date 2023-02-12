package net.countercraft.movecraft.combat.features;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.combat.MovecraftCombat;
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
                float value;
                if (entry.getValue() instanceof Float) {
                    value = (float) entry.getValue();
                }
                else if (entry.getValue() instanceof Integer) {
                    int intVal = (int) entry.getValue();
                    value = (float) intVal;
                }
                else {
                    MovecraftCombat.getInstance().getLogger().warning("Unable to load " + m.name() + ": " + entry.getValue());
                    continue;
                }
                BlastResistanceOverride.put(m, value);
            }
        }
    }

    private static Field getField() {
        return FieldUtils.getField(Material.class, "durability");
    }

    private static Class<?> getCraftMagicNumbers() throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getVersion() + ".util.CraftMagicNumbers");
    }

    private static Object getBlock(Material m) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        return getCraftMagicNumbers().getMethod("getBlock", Material.class).invoke(null, m);
    }

    private static String getVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static boolean setBlastResistance(Material m, float resistance) {
        try {
            FieldUtils.writeField(getField(), getBlock(m), resistance);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException e) {
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
        MovecraftCombat.getInstance().getLogger().info("Overwrote " + BlastResistanceOverride.keySet().size() + " blast resistances!");
    }

    public static void disable() {
        for (Material m : BlastResistanceOverride.keySet()) {
            revertToVanilla(m);
        }
    }
}
