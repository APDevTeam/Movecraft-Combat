package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.radar.RadarManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageManager;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;


public class CraftReleaseListener implements Listener {
    @EventHandler
    public void releaseListener(CraftReleaseEvent e) {
        StatusManager.getInstance().craftReleased(e);
        if(!(e.getCraft() instanceof PlayerCraft))
            return;

        PlayerCraft playerCraft = (PlayerCraft) e.getCraft();
        DamageManager.getInstance().craftReleased(playerCraft);

        Player p = playerCraft.getPlayer();
        RadarManager.getInstance().endPilot(p);
        RadarManager.getInstance().endInvisible(p);
    }
}
