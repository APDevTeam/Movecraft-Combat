package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    void playerQuitListener(PlayerQuitEvent e) {
        MovecraftCombat.getInstance().getPlayerManager().playerQuit(e.getPlayer());
    }
}