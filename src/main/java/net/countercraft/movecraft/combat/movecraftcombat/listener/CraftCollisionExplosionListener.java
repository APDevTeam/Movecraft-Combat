package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.damagetype.TorpedoDamage;
import net.countercraft.movecraft.craft.CraftManager;
//import net.countercraft.movecraft.util.CraftStatus;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.events.CraftCollisionExplosionEvent;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageManager;
import org.jetbrains.annotations.Nullable;

import java.util.Set;


public class CraftCollisionExplosionListener implements Listener {
    @EventHandler
    public void collisionExplosionListener(CraftCollisionExplosionEvent e) {
        if(!Config.EnableTorpedoTracking)
            return;
        if(e.getCraft().getNotificationPlayer() == null)
            return;

        //check if the craft should sink
        /* TODO: UNCOMMENT THIS BEFORE USING ANYWHERE!
        CraftStatus status = Movecraft.getInstance().getAsyncManager().checkCraftStatus(e.getCraft());
        if(status.isSinking()) {
            e.setCancelled(true);
            e.getCraft().setCruising(false);
            e.getCraft().sink();
        }
        */

        Craft craft = fastNearestCraftToCraft(e.getCraft());
        if(craft == null)
            return;
        if(craft == e.getCraft()) {
            return;
        }
        if(!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(e.getLocation())))
            return;

        DamageManager.getInstance().addDamageRecord(craft, e.getCraft().getNotificationPlayer(), new TorpedoDamage());
        StatusManager.getInstance().registerEvent(craft.getNotificationPlayer());
    }

    @Nullable
    private Craft fastNearestCraftToCraft(Craft c) {
        Location loc = c.getHitBox().getMidPoint().toBukkit(c.getW());
        Craft ret = null;
        long closestDistSquared = Long.MAX_VALUE;
        Set<Craft> craftsList = CraftManager.getInstance().getCraftsInWorld(c.getW());
        craftsList.remove(c);
        for (Craft i : craftsList) {
            int midX = (i.getHitBox().getMaxX() + i.getHitBox().getMinX()) >> 1;
            int midZ = (i.getHitBox().getMaxZ() + i.getHitBox().getMinZ()) >> 1;
            long distSquared = (long) (Math.pow(midX -  loc.getX(), 2) + Math.pow(midZ - (int) loc.getZ(), 2));
            if (distSquared < closestDistSquared) {
                closestDistSquared = distSquared;
                ret = i;
            }
        }
        return ret;
    }
}
