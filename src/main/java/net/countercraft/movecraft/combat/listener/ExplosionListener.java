package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.event.ExplosionDamagePlayerCraftEvent;
import net.countercraft.movecraft.combat.features.tracking.DamageTracking;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts an {@link EntityExplodeEvent} into an {@link ExplosionDamagePlayerCraftEvent}.
 */
public class ExplosionListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityExplodeEvent(@NotNull EntityExplodeEvent e) {
        if(!DamageTracking.EnableTNTTracking && !DamageTracking.EnableFireballTracking)
            return;

        Location loc = e.getLocation();
        PlayerCraft craft = fastNearestPlayerCraftToLoc(loc);
        if(craft == null)
            return;

        if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(loc))) {
            ExplosionDamagePlayerCraftEvent event = new ExplosionDamagePlayerCraftEvent(e.getEntity(), craft);
            Bukkit.getPluginManager().callEvent(event);
            return;
        }
        for(Block b : e.blockList()) {
            if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation()))) {
                ExplosionDamagePlayerCraftEvent event = new ExplosionDamagePlayerCraftEvent(e.getEntity(), craft);
                Bukkit.getPluginManager().callEvent(event);
                return;
            }
        }
   }

    @Nullable
    private PlayerCraft fastNearestPlayerCraftToLoc(@NotNull Location source) {
        MovecraftLocation loc = MathUtils.bukkit2MovecraftLoc(source);
        PlayerCraft closest = null;
        long closestDistSquared = Long.MAX_VALUE;
        for(Craft other : CraftManager.getInstance()) {
            if(other.getWorld() != source.getWorld())
                continue;
            if(!(other instanceof PlayerCraft))
                continue;

            long distSquared = other.getHitBox().getMidPoint().distanceSquared(loc);
            if(distSquared < closestDistSquared) {
                closestDistSquared = distSquared;
                closest = (PlayerCraft) other;
            }
        }
        return closest;
    }
}
