package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.features.CombatRelease;
import net.countercraft.movecraft.combat.features.damagetracking.DamageTracking;
import net.countercraft.movecraft.combat.features.damagetracking.FireballTracking;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;


public class ProjectileLaunchListener implements Listener {
    @EventHandler
    public void projectileLaunchEvent(ProjectileLaunchEvent e) {
        if(!(e.getEntity() instanceof SmallFireball))
            return;
        SmallFireball fireball = (SmallFireball) e.getEntity();

        if(!DamageTracking.EnableFireballTracking)
            return;

        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(fireball.getLocation());
        if(!(craft instanceof PlayerCraft))
            return;
        if(!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;

        PlayerCraft playerCraft = (PlayerCraft) craft;
        FireballTracking.getInstance().dispensedFireball(playerCraft, fireball);
        CombatRelease.getInstance().registerEvent(playerCraft.getPilot());
    }
}
