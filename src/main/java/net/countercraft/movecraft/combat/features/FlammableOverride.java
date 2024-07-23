package net.countercraft.movecraft.combat.features;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.util.Tags;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class FlammableOverride {

    public static Map<Material, Pair<Integer, Integer>> flammabilityOverride = null;
    private static net.countercraft.movecraft.combat.features.FlammableOverride.FlammabilityNMS nmsInterface;

    public static void load(@NotNull FileConfiguration config) {
        try {
            if (!config.contains("FlammabilityOverride"))
                return;
            var section = config.getConfigurationSection("FlammabilityOverride");
            if (section == null)
                return;

            flammabilityOverride = new HashMap<>();
            for (var entry : section.getValues(false).entrySet()) {
                EnumSet<Material> materials = Tags.parseMaterials(entry.getKey());
                for (Material m : materials) {
                    int flammability;
                    int encouragement;

                    String valStr = entry.getValue().toString();
                    try {
                        String[] split = valStr.split(",");
                        flammability = Integer.parseInt(split[0]);
                        encouragement = flammability;
                        if (split.length > 1) {
                            encouragement = Integer.parseInt(split[1]);
                        }
                    } catch (NumberFormatException | NullPointerException ex) {
                        MovecraftCombat.getInstance().getLogger()
                                .warning("Unable to load " + m.name() + ": " + entry.getValue());
                        continue;
                    }
                    flammabilityOverride.put(m, new Pair<>(flammability, encouragement));
                }
            }

            String[] parts = Bukkit.getServer().getMinecraftVersion().split("\\.");
            if (parts.length < 2)
                throw new IllegalArgumentException();
            int major_version = Integer.parseInt(parts[1]);
            if (major_version < 20) {
                nmsInterface = new FlammabilityNMS_SpigotMappings(); // Tested on 1.19.3
            } else {
                nmsInterface = new FlammabilityNMS_MojangMappings(); // Untested, uses field names of 1.20.1, so it should work
            }
        } catch (Exception e) {
            MovecraftCombat.getInstance().getLogger().info("Failed to load FlammabilityOverride: ");
            e.printStackTrace();
        }
    }

    public static boolean setFlammability(Material m, int flammability, int encouragement) {
        return nmsInterface.setFlammabilityProperties(m, flammability, encouragement);
    }

    public static boolean revertToVanilla(Material m) {
        return true;
        //return setFlammability(m, m.getBlastResistance()); // Spigot stores the vanilla values for us
    }

    public static void enable() {
        try {
            for (var entry : flammabilityOverride.entrySet()) {
                if (!setFlammability(entry.getKey(), entry.getValue().getLeft(), entry.getValue().getRight()))
                    MovecraftCombat.getInstance().getLogger()
                            .warning("Unable to set " + entry.getKey().name() + ": " + entry.getValue());
            }
        } catch (Exception e) {
            MovecraftCombat.getInstance().getLogger().info("Failed to enable FlammabilityOverride: ");
            e.printStackTrace();
        }
    }

    public static void disable() {
        try {
            for (Material m : flammabilityOverride.keySet()) {
                if (!revertToVanilla(m))
                    MovecraftCombat.getInstance().getLogger().warning("Unable to reset " + m.name());
            }
        } catch (Exception e) {
            MovecraftCombat.getInstance().getLogger().info("Failed to disable FlammabilityOverride: ");
            e.printStackTrace();
        }
    }

    private static class FlammabilityNMS {

        public boolean setFlammabilityProperties(Material m, int flammability, int encouragement) {
            throw new NotImplementedException();
        }

        protected static void writeField(@NotNull Object fireBlock, @NotNull Object block, String mapName, int value) throws IllegalAccessException, NoSuchFieldException {
            Field map = fireBlock.getClass().getDeclaredField(mapName);
            map.setAccessible(true);
            Object2IntMap mapObj = (Object2IntMap)map.get(fireBlock);
            mapObj.put(block, value);
        }
    }

    private static class FlammabilityNMS_SpigotMappings extends net.countercraft.movecraft.combat.features.FlammableOverride.FlammabilityNMS {
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

        public boolean setFlammabilityProperties(Material m, int flammability, int encouragement) {
            try {
                Object block = getBlockClass(getCraftMagicNumbersClass(), m);
                Object fireBlock = getBlockClass(getCraftMagicNumbersClass(), Material.FIRE);
                // First Object2Identiy map in field list is the one for ignite odds, second one is for burn odds
                writeField(fireBlock, block, "O", encouragement);
                writeField(fireBlock, block, "P", flammability);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | NoSuchFieldException
                     | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private static class FlammabilityNMS_MojangMappings extends net.countercraft.movecraft.combat.features.FlammableOverride.FlammabilityNMS {
        @NotNull
        protected static Class<?> getCraftMagicNumbersClass() throws ClassNotFoundException {
            return Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".util.CraftMagicNumbers");
        }

        protected static Object getBlockClass(@NotNull Class<?> magicNumbers, Material m)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            Method method = magicNumbers.getMethod("getBlock", Material.class);
            return method.invoke(null, m);
        }
        public boolean setFlammabilityProperties(Material m, int flammability, int encouragement) {
            try {
                Object block = getBlockClass(getCraftMagicNumbersClass(), m);
                Object fireBlock = getBlockClass(getCraftMagicNumbersClass(), Material.FIRE);
                writeField(fireBlock, block, "burnOdds", flammability);
                writeField(fireBlock, block, "igniteOdds", encouragement);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | NoSuchFieldException
                     | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
