package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.TNTTracking;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MathUtils;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;


public class DispenseListener implements Listener {
    @EventHandler
    public void dispenseEvent(EntitySpawnEvent e) {
        if(!(e.getEntity() instanceof TNTPrimed)) {
            return;
        }
        TNTPrimed tnt = (TNTPrimed) e.getEntity();

        Craft craft = MovecraftCombat.fastNearestCraftToLoc(e.getLocation());
        if(!craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getLocation()))) {
            return;
        }
        TNTTracking.getInstance().dispensedTNT(craft.getNotificationPlayer(), tnt);
    }
}
