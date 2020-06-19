package net.countercraft.movecraft.combat.movecraftcombat.listener;

import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.FireballTracking;


public class ProjectileHitListener implements Listener {
    @EventHandler
    public void projectileHitEvent(ProjectileHitEvent e) {
        if(!Config.EnableFireballTracking)
            return;
        if(e.getEntity() == null)
            return;
        if(!(e.getEntity() instanceof Fireball))
            return;
        Fireball fireball = (Fireball) e.getEntity();

        Craft craft = MovecraftCombat.fastNearestCraftToLoc(fireball.getLocation());
        if(craft == null)
            return;
        if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(fireball.getLocation()))) {
            FireballTracking.getInstance().damagedCraft(craft, fireball);
            return;
        }
        FireballTracking.getInstance().expiredFireball(fireball);
    }
}
