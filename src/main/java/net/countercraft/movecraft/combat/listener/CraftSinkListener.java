package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.radar.RadarManager;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.combat.tracking.DamageManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.countercraft.movecraft.events.CraftSinkEvent;


public class CraftSinkListener implements Listener {
    @EventHandler
    public void sinkListener(CraftSinkEvent e) {
        if(!(e.getCraft() instanceof PlayerCraft))
            return;

        PlayerCraft playerCraft = (PlayerCraft) e.getCraft();
        DamageManager.getInstance().craftSunk(playerCraft);
        StatusManager.getInstance().craftSunk(playerCraft);

        Player p = playerCraft.getPlayer();
        RadarManager.getInstance().endPilot(p);
        RadarManager.getInstance().endInvisible(p);
    }
}
