package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.combat.movecraftcombat.fireballs.FireballManager;
import net.countercraft.movecraft.craft.CraftManager;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.FireballTracking;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;


public class ProjectileLaunchListener implements Listener {
    @EventHandler
    public void projectileLaunchEvent(ProjectileLaunchEvent e) {
        if(!Config.EnableFireballTracking)
            return;

        if(!(e.getEntity() instanceof SmallFireball))
            return;
        SmallFireball fireball = (SmallFireball) e.getEntity();
        FireballManager.getInstance().addFireball(fireball);

        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(fireball.getLocation());
        if(craft == null)
            return;
        if(!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;

        FireballTracking.getInstance().dispensedFireball(craft, fireball);
        StatusManager.getInstance().registerEvent(craft.getNotificationPlayer());
    }
}