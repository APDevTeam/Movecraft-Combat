package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.radar.RadarManager;
import net.countercraft.movecraft.events.CraftPilotEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CraftPilotListener implements Listener {
    @EventHandler
    public void pilotListener(CraftPilotEvent e) {
        Player p = e.getCraft().getNotificationPlayer();
        RadarManager.getInstance().startPilot(p);
        RadarManager.getInstance().startInvisible(p);
    }
}