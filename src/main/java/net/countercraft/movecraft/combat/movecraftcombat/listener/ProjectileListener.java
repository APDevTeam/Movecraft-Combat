package net.countercraft.movecraft.combat.movecraftcombat.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.FireballTracking;

public class ProjectileListener implements Listener {
    @EventHandler
    public void projectileLaunchEvent(ProjectileLaunchEvent e) {
        Bukkit.broadcast("Fireball dispensed at " + e.getEntity().getLocation(), "movecraft.combat.debug");
        if(!(e.getEntity() instanceof Fireball))
            return;
        Fireball fireball = (Fireball) e.getEntity();

        Craft craft = MovecraftCombat.fastNearestCraftToLoc(fireball.getLocation());
        if(craft == null)
            return;
        if(!craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;
        Bukkit.broadcast("Found craft of " + craft.getNotificationPlayer().getDisplayName(), "movecraft.combat.debug");
        FireballTracking.getInstance().dispensedFireball(craft.getNotificationPlayer(), fireball);
    }
}