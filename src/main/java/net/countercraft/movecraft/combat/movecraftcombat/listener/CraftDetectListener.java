package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.radar.RadarManager;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CraftDetectListener implements Listener {
    @EventHandler
    public void pilotListener(CraftDetectEvent e) {
        Player p = e.getCraft().getNotificationPlayer();
        if(p == null) {
            return;
        }
        if(e.getCraft().getType().getCruiseOnPilot()) {
            return;
        }

        RadarManager.getInstance().startPilot(p);
        Craft c = e.getCraft();
        if(MathUtils.locIsNearCraftFast(c, MathUtils.bukkit2MovecraftLoc(p.getLocation()))) {
            RadarManager.getInstance().startInvisible(p);
        }
    }
}