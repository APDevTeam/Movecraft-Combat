package net.countercraft.movecraft.combat.features;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.util.Tags;

public class BlastResistanceOverride {
    public static Map<Material, Float> BlastResistanceOverride = null;
    private static BlastResistanceNMS nmsInterface;

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
                } else if (entry.getValue() instanceof Integer) {
                    int intVal = (int) entry.getValue();
                    value = (float) intVal;
                } else {
                    MovecraftCombat.getInstance().getLogger()
                            .warning("Unable to load " + m.name() + ": " + entry.getValue());
                    continue;
                }
                BlastResistanceOverride.put(m, value);
            }
        }

        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        int major_version = Integer.parseInt(version.split("_")[1]);
        if (major_version < 17) {
            nmsInterface = new BlastResistanceNMS_V1(); // Tested on 1.14.4 and 1.16.5
        } else if (major_version == 20) {
            nmsInterface = new BlastResistanceNMS_v1_20(); // Tested on 1.20.1
        } else {
            nmsInterface = new BlastResistanceNMS_V2(); // Tested on 1.19.4 and 1.18.2
        }
    }

    public static boolean setBlastResistance(Material m, float resistance) {
        return nmsInterface.setBlastResistance(m, resistance);
    }

    public static boolean revertToVanilla(Material m) {
        return setBlastResistance(m, m.getBlastResistance()); // Spigot stores the vanilla values for us
    }

    public static void enable() {
        for (var entry : BlastResistanceOverride.entrySet()) {
            if (!setBlastResistance(entry.getKey(), entry.getValue()))
                MovecraftCombat.getInstance().getLogger()
                        .warning("Unable to set " + entry.getKey().name() + ": " + entry.getValue());
        }
    }

    public static void disable() {
        for (Material m : BlastResistanceOverride.keySet()) {
            if (!revertToVanilla(m))
                MovecraftCombat.getInstance().getLogger().warning("Unable to reset " + m.name());
        }
    }

    private static class BlastResistanceNMS {
        public boolean setBlastResistance(Material m, float resistance) {
            throw new NotImplementedException();
        }

        protected static Class<?> getCraftMagicNumbersClass() throws ClassNotFoundException {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            String version = packageName.substring(packageName.lastIndexOf('.') + 1);
            return Class.forName("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers");
        }

        protected static Object getBlockClass(Class<?> magicNumbers, Material m)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            Method method = magicNumbers.getMethod("getBlock", Material.class);
            return method.invoke(null, m);
        }

        protected static void writeField(Object block, String fieldName, float resistance)
                throws IllegalAccessException {
            Field field = FieldUtils.getField(block.getClass(), fieldName, true);
            FieldUtils.writeField(field, block, resistance);
        }
    }

    private static class BlastResistanceNMS_V1 extends BlastResistanceNMS {
        public boolean setBlastResistance(Material m, float resistance) {
            try {
                Object block = getBlockClass(getCraftMagicNumbersClass(), m);
                writeField(block, "durability", resistance);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException
                    | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private static class BlastResistanceNMS_V2 extends BlastResistanceNMS {
        public boolean setBlastResistance(Material m, float resistance) {
            try {
                Object block = getBlockClass(getCraftMagicNumbersClass(), m);
                writeField(block, "aH", resistance); // obfuscated explosionResistance
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException
                    | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private static class BlastResistanceNMS_v1_20 extends BlastResistanceNMS {
        public boolean setBlastResistance(Material m, float resistance) {
            try {
                Object block = getBlockClass(getCraftMagicNumbersClass(), m);
                writeField(block, "aF", resistance); // obfuscated explosionResistance
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException
                    | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
