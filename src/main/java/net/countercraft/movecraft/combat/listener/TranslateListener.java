package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class TranslateListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void translateEvent(CraftTranslateEvent e) {
        if(!Config.MovementTracers)
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
            String setting = MovecraftCombat.getInstance().getPlayerManager().getSetting(p);
            if(setting == null || setting.equals("OFF") || setting.equals("LOW"))
                continue;

            new BukkitRunnable() {
                @Override
                public void run() {
                    for(var loc : difference)
                        p.spawnParticle(Config.TracerParticle, loc.toBukkit(w), 0, 0.0, 0.0, 0.0);
                }
            }.runTaskLater(MovecraftCombat.getInstance(), 1);
        }
    }
}
