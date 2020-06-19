package net.countercraft.movecraft.combat.movecraftcombat.listener;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.TrackingManager;


public class CraftReleaseListener implements Listener {
    @EventHandler
    public void releaseListener(CraftReleaseEvent e) {
        TrackingManager.getInstance().craftReleased(e.getCraft());
    }
}
