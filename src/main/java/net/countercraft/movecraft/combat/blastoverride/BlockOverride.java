package net.countercraft.movecraft.combat.blastoverride;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Overrides particular values on blocks,
 *
 * @author Kristian
 */
public class BlockOverride {

    // Reflexion classes
    private Class<?> CraftMagicNumbers = Class.forName("org.bukkit.craftbukkit." + BlastResistanceOverride.getVersion() + ".util.CraftMagicNumbers");
    // The block we will override
    private Object block;

    // Old values
    private Map<String, Object> oldValues = new HashMap<String, Object>();
    private Map<String, Field> fieldCache = new HashMap<String, Field>();

    public BlockOverride(Material material) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        this.block = CraftMagicNumbers.getMethod("getBlock", Material.class).invoke(null, material);
    }

    public boolean isValid(){return this.block != null;}
    /**
     * Update the given field with a new value.
     * @param fieldName - name of field.
     * @param object - new value.
     * @throws IllegalArgumentException If the field name is NULL or the field doesn't exist.
     * @throws RuntimeException If we don't have security clearance.
     */
    public void set(String fieldName, Object object) {

        try {
            // Write the value directly
            if (!oldValues.containsKey(fieldName)) {
                oldValues.put(fieldName, FieldUtils.readField(getField(fieldName), block));
            }
            FieldUtils.writeField(getField(fieldName), block, object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read field.", e);
        }
    }

    /**
     * Retrieves the current field value.
     * @param fieldName - name of field.
     * @throws IllegalArgumentException If the field name is NULL or the field doesn't exist.
     * @throws RuntimeException If we don't have security clearance.
     */
    public Object get(String fieldName) {
        try {
            // Read the value directly
            return FieldUtils.readField(getField(fieldName), block);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read field.", e);
        }
    }

    /**
     * Retrieves the old vanilla field value.
     * @param fieldName - name of field.
     * @throws IllegalArgumentException If the field name is NULL or the field doesn't exist.
     * @throws RuntimeException If we don't have security clearance.
     */
    public Object getVanilla(String fieldName) {
        if (fieldName == null)
            throw new NullArgumentException("fieldName");

        if (oldValues.containsKey(fieldName))
            return oldValues.get(fieldName);
        else
            return get(fieldName);
    }

    /**
     * Retrieves a immutable representation of the stored vanilla values.
     * @return Old values.
     */
    public ImmutableMap<String, Object> getVanillaValues() {
        return ImmutableMap.copyOf(oldValues);
    }

    /**
     * Reset everything to vanilla.
     */
    public void revertAll() {
        // Reset what we have
        for (String stored : oldValues.keySet()) {
            set(stored, getVanilla(stored));
        }

        // Remove list
        oldValues.clear();
    }

    /**
     * Called when we wish to persist every change, even when this class is garbage collected.
     */
    public void saveAll() {
        oldValues.clear();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        // We definitely should revert when we're done
        if (oldValues != null && oldValues.size() > 0) {
            revertAll();
        }
        super.finalize();
    }

    private Field getField(String fieldName) {

        Field cached = fieldCache.get(fieldName);

        if (cached == null) {
            cached = FieldUtils.getField(block.getClass(), fieldName, true);

            // Remember this particular field
            if (cached != null) {
                fieldCache.put(fieldName, cached);
            } else {
                throw new IllegalArgumentException("Cannot locate field " + fieldName);
            }
        }

        return cached;
    }
}
