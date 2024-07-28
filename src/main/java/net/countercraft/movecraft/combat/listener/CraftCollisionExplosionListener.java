package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.event.CollisionDamagePlayerCraftEvent;
import net.countercraft.movecraft.combat.features.tracking.DamageTracking;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftCollisionExplosionEvent;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts a {@link CraftCollisionExplosionEvent} into a {@link CollisionDamagePlayerCraftEvent}.
 */
public class CraftCollisionExplosionListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void collisionExplosionListener(@NotNull CraftCollisionExplosionEvent e) {
        if (!DamageTracking.EnableTorpedoTracking)
            return;
        if (!(e.getCraft() instanceof PilotedCraft damaging))
            return;

        PlayerCraft damaged = fastNearestPlayerCraftToCraft(damaging);
        if (damaged == null)
            return;
        if (!MathUtils.locIsNearCraftFast(damaged, MathUtils.bukkit2MovecraftLoc(e.getLocation())))
            return;

        var event = new CollisionDamagePlayerCraftEvent(damaging, damaged);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Nullable
    private PlayerCraft fastNearestPlayerCraftToCraft(@NotNull Craft source) {
        MovecraftLocation loc = source.getHitBox().getMidPoint();
        PlayerCraft closest = null;
        long closestDistSquared = Long.MAX_VALUE;
        for (Craft other : CraftManager.getInstance()) {
            if (other == source)
                continue;
            if (other.getWorld() != source.getWorld())
                continue;
            if (!(other instanceof PlayerCraft))
                continue;

            long distSquared = other.getHitBox().getMidPoint().distanceSquared(loc);
            if (distSquared < closestDistSquared) {
                closestDistSquared = distSquared;
                closest = (PlayerCraft) other;
            }
        }
        return closest;
    }
}
