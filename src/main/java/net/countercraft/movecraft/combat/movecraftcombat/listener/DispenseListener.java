package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MathUtils;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.FireballTracking;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.TNTTracking;


public class DispenseListener implements Listener {
    @EventHandler
    public void dispenseEvent(EntitySpawnEvent e) {
        processTNT(e);
        processFireball(e);
    }

    private void processTNT(EntitySpawnEvent e) {
        if(!(e.getEntity() instanceof TNTPrimed))
            return;
        TNTPrimed tnt = (TNTPrimed) e.getEntity();

        Craft craft = MovecraftCombat.fastNearestCraftToLoc(e.getLocation());
        if(!craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getLocation())))
            return;
        TNTTracking.getInstance().dispensedTNT(craft.getNotificationPlayer(), tnt);
    }

    private void processFireball(EntitySpawnEvent e) {
        if(!(e.getEntity() instanceof Fireball))
            return;
        Fireball fireball = (Fireball) e.getEntity();

        Craft craft = MovecraftCombat.fastNearestCraftToLoc(e.getLocation());
        if(!craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getLocation())))
            return;
        FireballTracking.getInstance().dispensedFireball(craft.getNotificationPlayer(), fireball);
    }
}
