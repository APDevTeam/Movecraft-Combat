package net.countercraft.movecraft.combat.blastoverride;

import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Overrides particular values on blocks,
 *
 * @author Kristian
 */
public class BlockOverride {

    // Reflection classe
    private final Material material;
    private final Class<?> CraftMagicNumbers = Class.forName("org.bukkit.craftbukkit." + BlastResistanceOverride.getVersion() + ".util.CraftMagicNumbers");
    // The block we will override
    private final Object block;

    public BlockOverride(Material material) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        this.material = material;
        block = CraftMagicNumbers.getMethod("getBlock", Material.class).invoke(null, material);
    }

    public boolean isValid(){return this.block != null;}
    /**
     * Override block's blast resistance.
     * @param br - new value.
     * @throws IllegalArgumentException If the field name is NULL or the field doesn't exist.
     * @throws RuntimeException If we don't have security clearance.
     */
    public void setBlastResistance(float br) {
        try {
            // Write the value directly
            FieldUtils.writeField(getField("durability"), block, br);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read field.", e);
        }
    }

    /**
     * Reset blast resistance to vanilla.
     */
    public void revertToVanilla() {
        setBlastResistance(material.getBlastResistance());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        // We definitely should revert when we're done
        revertToVanilla();
        super.finalize();
    }

    private Field getField(String fieldName) {
        return FieldUtils.getField(block.getClass(), fieldName, true);
    }
}
