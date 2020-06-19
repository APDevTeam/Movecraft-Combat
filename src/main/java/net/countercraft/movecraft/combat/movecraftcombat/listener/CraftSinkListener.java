package net.countercraft.movecraft.combat.movecraftcombat.listener;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.TrackingManager;


public class CraftSinkListener implements Listener {
    @EventHandler
    public void sinkListener(CraftSinkEvent e) {
        TrackingManager.getInstance().craftSunk(e.getCraft());
    }
}
