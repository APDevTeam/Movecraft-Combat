package net.countercraft.movecraft.combat.features.tracers;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.features.tracers.config.PlayerManager;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import static net.countercraft.movecraft.combat.features.tracers.TNTTracers.TracerParticle;

public class MovementTracers implements Listener {
    public static boolean MovementTracers = false;

    public static void load(@NotNull FileConfiguration config) {
        MovementTracers = config.getBoolean("MovementTracers", false);
    }


    @NotNull
    private final PlayerManager manager;


    public MovementTracers(@NotNull PlayerManager manager) {
        this.manager = manager;
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCraftTranslate(CraftTranslateEvent e) {
        if(!MovementTracers)
            return;
        if(e.getNewHitBox().isEmpty() || e.getOldHitBox().isEmpty())
            return;

        final World w = e.getCraft().getWorld();
        final HitBox difference = e.getOldHitBox().difference(e.getNewHitBox());
        Location center = e.getNewHitBox().getMidPoint().toBukkit(w);

        long maxDistSquared = Bukkit.getServer().getViewDistance() * 16L;
        maxDistSquared = maxDistSquared - 16;
        maxDistSquared = maxDistSquared * maxDistSquared;

        for(final Player p : e.getWorld().getPlayers()) {
            if(p.getLocation().distanceSquared(center) > maxDistSquared)
                continue;
            String setting = manager.getSetting(p);
            if(setting == null || setting.equals("OFF") || setting.equals("LOW"))
                continue;

            new BukkitRunnable() {
                @Override
                public void run() {
                    for(var loc : difference)
                        p.spawnParticle(TracerParticle, loc.toBukkit(w).add(0.5, 0.5, 0.5), 0, 0.0, 0.0, 0.0);
                }
            }.runTaskLater(MovecraftCombat.getInstance(), 1);
        }
    }
}
