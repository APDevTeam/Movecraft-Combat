package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class CraftReleaseListener implements Listener {
    @EventHandler
    public void releaseListener(CraftReleaseEvent e) {
        StatusManager.getInstance().craftReleased(e);
    }
}
