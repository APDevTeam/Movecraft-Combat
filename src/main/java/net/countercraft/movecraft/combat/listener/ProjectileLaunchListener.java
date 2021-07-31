package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.fireballs.FireballManager;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.combat.tracking.FireballTracking;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.craft.Craft;


public class ProjectileLaunchListener implements Listener {
    @EventHandler
    public void projectileLaunchEvent(ProjectileLaunchEvent e) {
        if(!(e.getEntity() instanceof SmallFireball))
            return;
        SmallFireball fireball = (SmallFireball) e.getEntity();
        FireballManager.getInstance().addFireball(fireball);

        if(!Config.EnableFireballTracking)
            return;

        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(fireball.getLocation());
        if(craft == null || !(craft instanceof PlayerCraft))
            return;
        if(!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;

        PlayerCraft playerCraft = (PlayerCraft) craft;
        FireballTracking.getInstance().dispensedFireball(playerCraft, fireball);
        StatusManager.getInstance().registerEvent(playerCraft.getPlayer());
    }
}