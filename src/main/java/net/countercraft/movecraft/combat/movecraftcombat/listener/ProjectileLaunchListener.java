package net.countercraft.movecraft.combat.movecraftcombat.listener;

import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.FireballTracking;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;


public class ProjectileLaunchListener implements Listener {
    @EventHandler
    public void projectileLaunchEvent(ProjectileLaunchEvent e) {
        if(!(e.getEntity() instanceof Fireball))
            return;
        Fireball fireball = (Fireball) e.getEntity();

        Craft craft = MovecraftCombat.fastNearestCraftToLoc(fireball.getLocation());
        if(craft == null)
            return;
        if(!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;
        FireballTracking.getInstance().dispensedFireball(craft, fireball);
        StatusManager.getInstance().registerEvent(craft.getNotificationPlayer());
    }
}