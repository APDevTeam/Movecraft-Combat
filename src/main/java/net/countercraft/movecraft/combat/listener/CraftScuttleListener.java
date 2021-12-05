package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.events.CraftScuttleEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;


public class CraftScuttleListener implements Listener {
    @EventHandler
    public void scuttleListener(CraftScuttleEvent e) {
        if(!Config.EnableCombatReleaseTracking)
            return;

        if(e.getCraft().getNotificationPlayer() != e.getCause())
            return; //  Always let /scuttle [player] run.

        if(!StatusManager.getInstance().isInCombat(e.getCause()))
            return;

        e.setCancelled(true);
        e.getCause().sendMessage(ERROR_PREFIX + " You may not scuttle while in combat!");
    }
}