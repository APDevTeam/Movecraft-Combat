package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.fireballs.FireballManager;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnListener implements Listener {
    @EventHandler
    public void entitySpawnEvent(EntitySpawnEvent e) {
        if(!(e.getEntity() instanceof SmallFireball)) {
            return;
        }
        SmallFireball f = (SmallFireball) e.getEntity();

        FireballManager.getInstance().addFireball(f);
    }
}
