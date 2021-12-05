package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.MovecraftCombat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    void playerQuitListener(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        MovecraftCombat.getInstance().getPlayerManager().playerQuit(p);
    }
}