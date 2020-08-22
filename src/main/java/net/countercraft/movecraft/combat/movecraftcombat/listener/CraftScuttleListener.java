package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.combat.movecraftcombat.radar.RadarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.countercraft.movecraft.events.CraftScuttleEvent;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;

import static net.countercraft.movecraft.utils.ChatUtils.ERROR_PREFIX;


public class CraftScuttleListener implements Listener {
    @EventHandler
    public void scuttleListener(CraftScuttleEvent e) {
        handleCombatScuttle(e);
        handleRadarScuttle(e);
    }

    private void handleCombatScuttle(CraftScuttleEvent e) {
        if(!Config.EnableCombatReleaseTracking)
            return;

        if(e.getCraft().getNotificationPlayer() != e.getCause())
            return; //  Always let /scuttle [player] run.

        if(!StatusManager.getInstance().isInCombat(e.getCause()))
            return;

        e.setCancelled(true);
        e.getCause().sendMessage(ERROR_PREFIX + " You may not scuttle while in combat!");
    }

    private void handleRadarScuttle(CraftScuttleEvent e) {
        if(e.isCancelled()) {
            return;
        }

        Player p = e.getCause();
        if(p == null)
            return;
        if(e.getCraft().getType().getCruiseOnPilot())
            return;

        RadarManager.getInstance().endInvisible(p);
        RadarManager.getInstance().endPilot(p);
    }
}