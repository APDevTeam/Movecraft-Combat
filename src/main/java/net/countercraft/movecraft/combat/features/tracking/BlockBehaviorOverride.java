package net.countercraft.movecraft.combat.features.tracking;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.features.BlastResistanceOverride;
import net.countercraft.movecraft.util.Tags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class BlockBehaviorOverride {

    public record BlockOverride(
            Optional<Float> blastResistanceOverride,
            Optional<Integer> burnOddity,
            Optional<Integer> igniteOddity
    ) {

    }

    public static Map<Material, BlockOverride> BLOCK_OVERRIDES = null;

    public static void load(@NotNull FileConfiguration config) {

    }

    private static abstract class NMSHelper {

        private final String fieldNameBlastResistance;
        private final String fieldNameBurnOdds;
        private final String fieldNameIgniteOdds;
        @NotNull Class<?> magicNumbers;

        protected NMSHelper(String blastResField, String burnOddsField, String igniteOddsField) {
            this.fieldNameBlastResistance = blastResField;
            this.fieldNameBurnOdds = burnOddsField;
            this.fieldNameIgniteOdds = igniteOddsField;
        }

        public boolean setBlastResistance(Material m, float value) {
            try {
                Object block = this.getBlockClass(m);
                writeField(block, value, this.fieldNameBlastResistance);
                return true;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | SecurityException | NoSuchFieldException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean setBurnOdds(Material m, int value) {
            try {
                Object fireBlock = this.getBlockClass(Material.FIRE);
                return setBurnOdds(m, value, fireBlock);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean setIgniteOdds(Material m, int value) {
            try {
                Object fireBlock = this.getBlockClass(Material.FIRE);
                return setIgniteOdds(m, value, fireBlock);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        protected boolean setBurnOdds(Material m, int value, Object fireBlock) {
            try {
                final Object block = getBlockClass(m);
                // First Object2Identiy map in field list is the one for ignite odds, second one is for burn odds
                Consumer<Object2IntMap> func = (map) -> {
                    map.put(block, value);
                };
                NMSSpigotMappings.writeField(fireBlock, func, this.fieldNameBurnOdds);
                return true;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | NoSuchFieldException
                     | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }
        protected boolean setIgniteOdds(Material m, int value, Object fireBlock) {
            try {
                final Object block = getBlockClass(m);
                // First Object2Identiy map in field list is the one for ignite odds, second one is for burn odds
                Consumer<Object2IntMap> func = (map) -> {
                    map.put(block, value);
                };
                NMSSpigotMappings.writeField(fireBlock, func, this.fieldNameIgniteOdds);
                return true;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | NoSuchFieldException
                     | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        abstract Class<?> getCraftMagicNumbersClass() throws ClassNotFoundException;

        protected Object getBlockClass(Material m)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
            if (this.magicNumbers == null) {
                this.magicNumbers = this.getCraftMagicNumbersClass();
            }
            Method method = magicNumbers.getMethod("getBlock", Material.class);
            return method.invoke(null, m);
        }

        protected static <T> void writeField(@NotNull Object block, @NotNull Consumer<T> whatToDoWithField, String mapName) throws IllegalAccessException, NoSuchFieldException, ClassCastException {
            Field map = block.getClass().getDeclaredField(mapName);
            map.setAccessible(true);
            T obj = (T)map.get(block);
            whatToDoWithField.accept(obj);
        }

        protected static <T> void writeField(@NotNull Object block, T value, String mapName) throws IllegalAccessException, NoSuchFieldException, ClassCastException {
            Field field = block.getClass().getDeclaredField(mapName);
            field.setAccessible(true);
            T obj = (T)field.get(block);
            field.set(block, value);
        }
    }

    private static class NMSSpigotMappings extends NMSHelper {

        private static final String FIELD_NAME_IGNITE_ODDS = "O";
        private static final String FIELD_NAME_BURN_ODDS = "P";
        private static final String FIELD_NAME_BLAST_RESISTANCE = "aH";

        public NMSSpigotMappings() {
            this(FIELD_NAME_BLAST_RESISTANCE, FIELD_NAME_BURN_ODDS, FIELD_NAME_IGNITE_ODDS);
        }

        protected NMSSpigotMappings(String blastResField, String burnOddsField, String igniteOddsField) {
            super(blastResField, burnOddsField, igniteOddsField);
        }

        @Override
        Class<?> getCraftMagicNumbersClass() throws ClassNotFoundException {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            String version = packageName.substring(packageName.lastIndexOf('.') + 1);
            return Class.forName("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers");
        }
    }

    private static class NMSMojangMappings extends NMSHelper {

        private static final String FIELD_NAME_IGNITE_ODDS = "igniteOdds";
        private static final String FIELD_NAME_BURN_ODDS = "burnOdds";
        private static final String FIELD_NAME_BLAST_RESISTANCE = "explosionResistance";

        public NMSMojangMappings() {
            this(FIELD_NAME_BLAST_RESISTANCE, FIELD_NAME_BURN_ODDS, FIELD_NAME_IGNITE_ODDS);
        }

        protected NMSMojangMappings(String blastResField, String burnOddsField, String igniteOddsField) {
            super(blastResField, burnOddsField, igniteOddsField);
        }

        @Override
        Class<?> getCraftMagicNumbersClass() throws ClassNotFoundException {
            return Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".util.CraftMagicNumbers");
        }
    }

}
