package net.countercraft.movecraft.combat.features.tracking;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.util.Tags;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class BlockBehaviorOverride {

    public record BlockOverride(
            Optional<Float> blastResistanceOverride,
            Optional<Integer> burnOddity,
            Optional<Integer> igniteOddity,
            Optional<Float> vanillaBlastResistanceOverride,
            Optional<Integer> vanillaBurnOddity,
            Optional<Integer> vanillaIgniteOddity
    ) {

    }

    public static Map<Material, BlockOverride> BLOCK_OVERRIDES = new HashMap<>();

    protected static final NMSHelper NMS_HELPER = NMSHelper.createInstance();

    public static void load(@NotNull FileConfiguration config) {
        final Set<Material> materialList = new HashSet<>();
        final Map<Material, Float> blastResMapping = new HashMap<>();
        final Map<Material, Pair<Integer, Optional<Integer>>> burnOddsMapping = new HashMap<>();
        final Map<Material, Pair<Integer, Optional<Integer>>> igniteOddsMapping = new HashMap<>();

        // First: Collect them all
        loadBlastResistanceValues(config, materialList::add, blastResMapping::put);
        loadFlammabilityValues(config, materialList::add, burnOddsMapping::put, igniteOddsMapping::put);

        // Second: Loop over and create the override objects
        for(Material m : materialList) {
            Optional<Float> blastResOverride = Optional.empty();
            Optional<Float> blastResVanilla = Optional.empty();
            Optional<Integer> burnOddsOverride = Optional.empty();
            Optional<Integer> burnOddsVanilla = Optional.empty();
            Optional<Integer> igniteOddsOverride = Optional.empty();
            Optional<Integer> igniteOddsVanilla = Optional.empty();

            if (blastResMapping.containsKey(m)) {
                blastResOverride = Optional.ofNullable(blastResMapping.getOrDefault(m, null));
                blastResVanilla = Optional.of(m.getBlastResistance());
            }

            if (burnOddsMapping.containsKey(m)) {
                burnOddsOverride = Optional.ofNullable(burnOddsMapping.get(m).left());
                burnOddsVanilla = burnOddsMapping.get(m).right();
            }

            if (igniteOddsMapping.containsKey(m)) {
                igniteOddsOverride = Optional.ofNullable(igniteOddsMapping.get(m).left());
                igniteOddsVanilla = igniteOddsMapping.get(m).right();
            }

            BlockOverride override = new BlockOverride(blastResOverride, burnOddsOverride, igniteOddsOverride, blastResVanilla, burnOddsVanilla, igniteOddsVanilla);
            BLOCK_OVERRIDES.put(m, override);
        }
    }

    protected static void loadFlammabilityValues(FileConfiguration config, Function<Material, Boolean> addToSet, BiConsumer<Material, Pair<Integer, Optional<Integer>>> putBurnOddsFunction, BiConsumer<Material, Pair<Integer, Optional<Integer>>> putIgniteOddsFunction) {
        if (!config.contains("FlammabilityOverride"))
            return;
        var section = config.getConfigurationSection("FlammabilityOverride");
        if (section == null)
            return;

        for (var entry : section.getValues(false).entrySet()) {
            EnumSet<Material> materials = Tags.parseMaterials(entry.getKey());
            for (Material m : materials) {
                int burnOddOverride = -1;
                Optional<Integer> burnOddVanilla = NMS_HELPER.getBurnOdds(m);
                int igniteOddOverride = -1;
                Optional<Integer> igniteOddVanilla = NMS_HELPER.getIgniteOdds(m);

                String valStr = entry.getValue().toString();
                try {
                    String[] split = valStr.split(",");
                    burnOddOverride = Integer.parseInt(split[0]);
                    igniteOddOverride = burnOddOverride;
                    if (split.length > 1) {
                        igniteOddOverride = Integer.parseInt(split[1]);
                    }
                } catch (NumberFormatException | NullPointerException ex) {
                    MovecraftCombat.getInstance().getLogger()
                            .warning("Unable to load " + m.name() + ": " + entry.getValue());
                    continue;
                }
                addToSet.apply(m);
                if (burnOddOverride >= 0) {
                    putBurnOddsFunction.accept(m, Pair.of(burnOddOverride, burnOddVanilla));
                }
                if (igniteOddOverride >= 0) {
                    putIgniteOddsFunction.accept(m, Pair.of(igniteOddOverride, igniteOddVanilla));
                }
            }
        }
    }

    protected static void loadBlastResistanceValues(FileConfiguration config, Function<Material, Boolean> addToSet, BiConsumer<Material, Float> putFunction) {
        if (!config.contains("BlastResistanceOverride"))
            return;
        var section = config.getConfigurationSection("BlastResistanceOverride");
        if (section == null)
            return;

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
                addToSet.apply(m);
                putFunction.accept(m, value);
            }
        }
    }

    public static void enable() {
        processOverriddenEntry(BlockBehaviorOverride::set, "Failed to set block overrides!");
    }

    public static void disable() {
        processOverriddenEntry(BlockBehaviorOverride::reset, "Failed to revert block overrides to vanilla!");
    }

    protected static void processOverriddenEntry(final BiFunction<Material, BlockOverride, Boolean> function, final String msgOnException) {
        try {
            for (Map.Entry<Material, BlockOverride> entry : BLOCK_OVERRIDES.entrySet()) {
                if (!function.apply(entry.getKey(), entry.getValue()))
                    MovecraftCombat.getInstance().getLogger().warning("Unable to set " + entry.getKey().name());
            }
        } catch (Exception e) {
            MovecraftCombat.getInstance().getLogger().info(msgOnException);
            e.printStackTrace();
        }
    }

    protected static boolean set(Material mat, BlockOverride override) {
        boolean result = true;

        // Blast resistance
        if (override.blastResistanceOverride().isPresent()) {
            result = result && NMS_HELPER.setBlastResistance(mat, override.blastResistanceOverride().get().floatValue());
        }
        // Burn oddity
        if (override.burnOddity().isPresent()) {
            result = result && NMS_HELPER.setBurnOdds(mat, override.burnOddity().get().intValue());
        }
        // Ignite oddity
        if (override.igniteOddity().isPresent()) {
            result = result && NMS_HELPER.setIgniteOdds(mat, override.igniteOddity().get().intValue());
        }

        return result;
    }

    protected static boolean reset(Material mat, BlockOverride override) {
        boolean result = true;

        // Blast resistance
        if (override.blastResistanceOverride().isPresent() && override.vanillaBlastResistanceOverride().isPresent()) {
            result = result && NMS_HELPER.setBlastResistance(mat, override.vanillaBlastResistanceOverride().get().floatValue());
        }
        // Burn oddity
        if (override.burnOddity().isPresent() && override.vanillaBurnOddity().isPresent()) {
            result = result && NMS_HELPER.setBurnOdds(mat, override.vanillaBlastResistanceOverride().get().intValue());
        }
        // Ignite oddity
        if (override.igniteOddity().isPresent() && override.vanillaIgniteOddity().isPresent()) {
            result = result && NMS_HELPER.setIgniteOdds(mat, override.vanillaBlastResistanceOverride().get().intValue());
        }

        return result;
    }

    private static abstract class NMSHelper {

        public static final NMSHelper createInstance() {
            String[] parts = Bukkit.getServer().getMinecraftVersion().split("\\.");
            if (parts.length < 2)
                throw new IllegalArgumentException();
            int major_version = Integer.parseInt(parts[1]);
            NMSHelper result;
            if (major_version < 20) {
                result = new NMSSpigotMappings();
            } else {
                result = new NMSMojangMappings();
            }
            return result;
        }

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

        public Optional<Float> getBlastResistance(Material m) {
            try {
                Object block = this.getBlockClass(m);
                return getFieldValueSafe(block, this.fieldNameBlastResistance);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return Optional.empty();
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

        public Optional<Integer> getBurnOdds(Material m) {
            try {
                Object fireBlock = this.getBlockClass(Material.FIRE);
                Object block = this.getBlockClass(m);
                Optional<Object2IntMap> optMap = getFieldValueSafe(fireBlock, this.fieldNameBurnOdds);
                if (optMap.isPresent()) {
                    if (optMap.get().containsKey(block)) {
                        return Optional.ofNullable(optMap.get().getInt(block));
                    }
                    return Optional.empty();
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return Optional.empty();
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

        public Optional<Integer> getIgniteOdds(Material m) {
            try {
                Object fireBlock = this.getBlockClass(Material.FIRE);
                Object block = this.getBlockClass(m);
                Optional<Object2IntMap> optMap = getFieldValueSafe(fireBlock, this.fieldNameIgniteOdds);
                if (optMap.isPresent()) {
                    if (optMap.get().containsKey(block)) {
                        return Optional.ofNullable(optMap.get().getInt(block));
                    }
                    return Optional.empty();
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return Optional.empty();
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

        protected static <T> void writeField(@NotNull Object block, @NotNull Consumer<T> whatToDoWithField, String fieldName) throws IllegalAccessException, NoSuchFieldException, ClassCastException {
            Field field = FieldUtils.getField(block.getClass(), fieldName, true);
            T obj = (T)field.get(block);
            whatToDoWithField.accept(obj);
        }

        protected static <T> void writeField(@NotNull Object block, T value, String fieldName) throws IllegalAccessException, NoSuchFieldException, ClassCastException {
            Field field = FieldUtils.getField(block.getClass(), fieldName, true);
            T obj = (T)field.get(block);
            field.set(block, value);
        }

        protected static <T> Optional<T> getFieldValueSafe(@NotNull Object instance, String fieldName) {
            try {
                return Optional.ofNullable(getFieldValue(instance, fieldName));
            } catch(Exception ex) {
                return Optional.empty();
            }
        }
        protected static <T> T getFieldValue(@NotNull Object instance, String fieldName) throws IllegalAccessException, NoSuchFieldException, ClassCastException {
            Field field = FieldUtils.getField(instance.getClass(), fieldName, true);
            T obj = (T)field.get(instance);
            return obj;
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
