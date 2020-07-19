package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;
import net.countercraft.movecraft.craft.CraftManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.events.CraftCollisionExplosionEvent;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageManager;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageType;


public class CraftCollisionExplosionListener implements Listener {
    @EventHandler
    public void collisionExplosionListener(CraftCollisionExplosionEvent e) {
        if(!Config.EnableTorpedoTracking)
            return;
        if(e.getCraft().getNotificationPlayer() == null)
            return;

        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getLocation());
        if(craft == null)
            return;
        if(craft == e.getCraft()) {
            Bukkit.broadcastMessage("uh oh, found same craft!");
            return;
        }
        if(!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(e.getLocation())))
            return;

        DamageManager.getInstance().addDamageRecord(craft, e.getCraft().getNotificationPlayer(), DamageType.TORPEDO);
        StatusManager.getInstance().registerEvent(craft.getNotificationPlayer());
        Bukkit.broadcastMessage("Torp hit craft!");
    }
}