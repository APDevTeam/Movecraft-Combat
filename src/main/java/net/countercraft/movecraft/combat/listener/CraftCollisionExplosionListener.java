package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.combat.tracking.damagetype.TorpedoDamage;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CraftStatus;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.events.CraftCollisionExplosionEvent;
import net.countercraft.movecraft.combat.tracking.DamageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;


public class CraftCollisionExplosionListener implements Listener {
    @EventHandler
    public void collisionExplosionListener(CraftCollisionExplosionEvent e) {
        if(!Config.EnableTorpedoTracking)
            return;
        if(!(e.getCraft() instanceof PilotedCraft))
            return;

        PilotedCraft damaging = (PilotedCraft) e.getCraft();

        //check if the craft should sink
        CraftStatus status = Movecraft.getInstance().getAsyncManager().checkCraftStatus(e.getCraft());
        if(status.isSinking()) {
            e.setCancelled(true);
            e.getCraft().setCruising(false);
            e.getCraft().sink();
        }

        PlayerCraft damaged = fastNearestPlayerCraftToCraft(damaging);
        if(damaged == null)
            return;
        if(!MathUtils.locIsNearCraftFast(damaged, MathUtils.bukkit2MovecraftLoc(e.getLocation())))
            return;

        DamageManager.getInstance().addDamageRecord(damaged, damaging.getPilot(), new TorpedoDamage());
        StatusManager.getInstance().registerEvent(damaged.getPilot());
    }

    @Nullable
    private PlayerCraft fastNearestPlayerCraftToCraft(@NotNull Craft source) {
        MovecraftLocation loc = source.getHitBox().getMidPoint();
        PlayerCraft closest = null;
        long closestDistSquared = Long.MAX_VALUE;
        for (Craft other : CraftManager.getInstance()) {
            if(other == source)
                continue;
            if(other.getWorld() != source.getWorld())
                continue;
            if(!(other instanceof PlayerCraft))
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
