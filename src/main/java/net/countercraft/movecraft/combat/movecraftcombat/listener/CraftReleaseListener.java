package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.radar.RadarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageManager;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;


public class CraftReleaseListener implements Listener {
    @EventHandler
    public void releaseListener(CraftReleaseEvent e) {
        DamageManager.getInstance().craftReleased(e.getCraft());
        StatusManager.getInstance().craftReleased(e);
        Player p = e.getCraft().getNotificationPlayer();
        RadarManager.getInstance().endPilot(p);
        RadarManager.getInstance().endInvisible(p);
    }
}
