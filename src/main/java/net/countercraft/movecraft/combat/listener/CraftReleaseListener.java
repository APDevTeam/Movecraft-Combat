package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.radar.RadarManager;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.combat.tracking.DamageManager;


public class CraftReleaseListener implements Listener {
    @EventHandler
    public void releaseListener(CraftReleaseEvent e) {
        StatusManager.getInstance().craftReleased(e);
        if(!(e.getCraft() instanceof PlayerCraft))
            return;

        PlayerCraft playerCraft = (PlayerCraft) e.getCraft();
        DamageManager.getInstance().craftReleased(playerCraft);

        Player p = playerCraft.getPilot();
        RadarManager.getInstance().endPilot(p);
        RadarManager.getInstance().endInvisible(p);
    }
}
