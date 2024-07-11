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
        try {
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
                    String valStr = entry.getValue().toString();
                    try {
                        value = Float.parseFloat(valStr);
                    } catch (NumberFormatException | NullPointerException ex) {
                        MovecraftCombat.getInstance().getLogger()
                                .warning("Unable to load " + m.name() + ": " + entry.getValue());
                        continue;
                    }
                    BlastResistanceOverride.put(m, value);
                }
            }

            String[] parts = Bukkit.getServer().getMinecraftVersion().split("\\.");
            if (parts.length < 2)
                throw new IllegalArgumentException();
            int major_version = Integer.parseInt(parts[1]);
            if (major_version < 20) {
                nmsInterface = new BlastResistanceNMS_SpigotMappings(); // Tested on 1.18.2 and 1.19.4
            } else {
                nmsInterface = new BlastResistanceNMS_MojangMappings(); // Tested on 1.20.6
            }
        } catch (Exception e) {
            MovecraftCombat.getInstance().getLogger().info("Failed to load BlastResistanceOverride: ");
            e.printStackTrace();
        }
    }

    public static boolean setBlastResistance(Material m, float resistance) {
        return nmsInterface.setBlastResistance(m, resistance);
    }

    public static boolean revertToVanilla(Material m) {
        return setBlastResistance(m, m.getBlastResistance()); // Spigot stores the vanilla values for us
    }

    public static void enable() {
        try {
            for (var entry : BlastResistanceOverride.entrySet()) {
                if (!setBlastResistance(entry.getKey(), entry.getValue()))
                    MovecraftCombat.getInstance().getLogger()
                            .warning("Unable to set " + entry.getKey().name() + ": " + entry.getValue());
            }
        } catch (Exception e) {
            MovecraftCombat.getInstance().getLogger().info("Failed to enable BlastResistanceOverride: ");
            e.printStackTrace();
        }
    }

    public static void disable() {
        try {
            for (Material m : BlastResistanceOverride.keySet()) {
                if (!revertToVanilla(m))
                    MovecraftCombat.getInstance().getLogger().warning("Unable to reset " + m.name());
            }
        } catch (Exception e) {
            MovecraftCombat.getInstance().getLogger().info("Failed to disable BlastResistanceOverride: ");
            e.printStackTrace();
        }
    }

    private static class BlastResistanceNMS {
        public boolean setBlastResistance(Material m, float resistance) {
            throw new NotImplementedException();
        }

        protected static void writeField(@NotNull Object block, String fieldName, float resistance)
                throws IllegalAccessException {
            Field field = FieldUtils.getField(block.getClass(), fieldName, true);
            FieldUtils.writeField(field, block, resistance);
        }
    }

    private static class BlastResistanceNMS_SpigotMappings extends BlastResistanceNMS {
        @NotNull
        protected static Class<?> getCraftMagicNumbersClass() throws ClassNotFoundException {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            String version = packageName.substring(packageName.lastIndexOf('.') + 1);
            return Class.forName("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers");
        }

        protected static Object getBlockClass(@NotNull Class<?> magicNumbers, Material m)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            Method method = magicNumbers.getMethod("getBlock", Material.class);
            return method.invoke(null, m);
        }

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

    private static class BlastResistanceNMS_MojangMappings extends BlastResistanceNMS {
        @NotNull
        protected static Class<?> getCraftMagicNumbersClass() throws ClassNotFoundException {
            return Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".util.CraftMagicNumbers");
        }

        protected static Object getBlockClass(@NotNull Class<?> magicNumbers, Material m)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            Method method = magicNumbers.getMethod("getBlock", Material.class);
            return method.invoke(null, m);
        }
        public boolean setBlastResistance(Material m, float resistance) {
            try {
                Object block = getBlockClass(getCraftMagicNumbersClass(), m);
                writeField(block, "explosionResistance", resistance);
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
