package net.countercraft.movecraft.combat.features;

import net.countercraft.movecraft.util.Tags;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    private static final Random random = new Random();

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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent e) {
        if (DurabilityOverride == null)
            return;
        if (e.getEntityType() != EntityType.PRIMED_TNT)
            return;

        Set<Block> removeList = new HashSet<>();
        for (Block b : e.blockList()) {
            if (!DurabilityOverride.containsKey(b.getType()))
                continue;

            // Generate a random number based on the location and system time
            long seed = (long) b.getX() * b.getY() * b.getZ() + (System.currentTimeMillis() >> 12);
            random.setSeed(seed); //don't create random each time just change the seed
            int chance = random.nextInt(100);
            if (chance > DurabilityOverride.get(b.getType()))
                continue;

            removeList.add(b);
        }
        e.blockList().removeAll(removeList);
    }
}
