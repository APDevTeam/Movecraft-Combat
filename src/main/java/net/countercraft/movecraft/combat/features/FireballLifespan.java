package net.countercraft.movecraft.combat.features;

import net.countercraft.movecraft.combat.MovecraftCombat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.LinkedList;

public class FireballLifespan extends BukkitRunnable implements Listener {
    private static final String METADATA_KEY = "MCC-Expiry";
    public static int FireballLifespan = 0;
    private final Deque<SmallFireball> queue = new LinkedList<>();

    public static void load(@NotNull FileConfiguration config) {
        FireballLifespan = config.getInt("FireballLifespan", 6);
        FireballLifespan *= 20 * 50; // Convert from seconds to milliseconds
    }

    @Override
    public void run() {
        // Clear out the old fireballs from the queue
        while (queue.size() > 0) {
            if (System.currentTimeMillis() - queue.peek().getMetadata(METADATA_KEY).get(0).asLong() <= FireballLifespan)
                break; // We've hit an older fireball, stop processing

            SmallFireball f = queue.pop();
            f.remove();
        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileLaunch(@NotNull ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof SmallFireball))
            return;

        SmallFireball fireball = (SmallFireball) e.getEntity();
        fireball.setMetadata(METADATA_KEY, new FixedMetadataValue(MovecraftCombat.getInstance(), System.currentTimeMillis()));
        queue.add(fireball);
    }
}
