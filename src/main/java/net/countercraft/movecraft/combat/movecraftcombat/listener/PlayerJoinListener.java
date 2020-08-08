package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    void playerJoinListener(PlayerJoinEvent e) {
        MovecraftCombat.getInstance().getPlayerManager().playerJoin(e.getPlayer());
    }
}
