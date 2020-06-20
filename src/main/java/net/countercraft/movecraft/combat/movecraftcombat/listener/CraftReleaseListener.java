package net.countercraft.movecraft.combat.movecraftcombat.listener;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageManager;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;


public class CraftReleaseListener implements Listener {
    @EventHandler
    public void releaseListener(CraftReleaseEvent e) {
        DamageManager.getInstance().craftReleased(e.getCraft());
        StatusManager.getInstance().craftReleased(e.getCraft(), e.getReason());
    }
}
