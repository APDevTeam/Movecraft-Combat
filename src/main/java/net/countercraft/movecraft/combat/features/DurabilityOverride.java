package net.countercraft.movecraft.combat.features;

import net.countercraft.movecraft.util.Tags;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DurabilityOverride implements Listener {
    public static Map<Material, Integer> DurabilityOverride = null;

    public static void load(@NotNull FileConfiguration config) {
        if (!config.contains("DurabilityOverride"))
            return;
        var section = config.getConfigurationSection("DurabilityOverride");
        if (section == null)
            return;

        DurabilityOverride = new HashMap<>();
        for (var entry : section.getValues(false).entrySet()) {
            EnumSet<Material> materials = Tags.parseMaterials(entry.getKey());
            for (Material m : materials)
                DurabilityOverride.put(m, (Integer) entry.getValue());
        }
    }


    private boolean nextToAir(@NotNull Block b) {
        return b.getRelative(BlockFace.UP).isEmpty() || b.getRelative(BlockFace.DOWN).isEmpty()
                || b.getRelative(BlockFace.EAST).isEmpty() || b.getRelative(BlockFace.WEST).isEmpty()
                || b.getRelative(BlockFace.NORTH).isEmpty() || b.getRelative(BlockFace.SOUTH).isEmpty();
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent e) {
        if (DurabilityOverride == null)
            return;
        if (e.getEntityType() != EntityType.TNT)
            return;

        Set<Block> removeList = new HashSet<>();
        for (Block b : e.blockList()) {
            // remove the block if no adjacent blocks are air (IE: the explosion skipped a block)
            if (!nextToAir(b)) {
                removeList.add(b);
                continue;
            }

            if (!DurabilityOverride.containsKey(b.getType()))
                continue;

            // Generate a random number based on the location and system time
            long seed = (long) b.getX() * b.getY() * b.getZ() + (System.currentTimeMillis() >> 12);
            int chance = new Random(seed).nextInt(100);
            if (chance > DurabilityOverride.get(b.getType()))
                continue;

            removeList.add(b);
        }
        e.blockList().removeAll(removeList);
    }
}
