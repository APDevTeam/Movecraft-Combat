package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.radar.RadarManager;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementListener implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if(!RadarManager.getInstance().isPilot(p)) {
            return;
        }

        Craft c = CraftManager.getInstance().getCraftByPlayer(p);
        if(c == null) {
            return;
        }
        boolean from = MathUtils.locIsNearCraftFast(c, MathUtils.bukkit2MovecraftLoc(e.getFrom()));
        boolean to;
        if(e.getTo() == null) {
            to = from;
        }
        else {
            to = MathUtils.locIsNearCraftFast(c, MathUtils.bukkit2MovecraftLoc(e.getTo()));
        }

        if(from && !to) {
            // Player left their craft
            RadarManager.getInstance().endInvisible(p);
        }
        else if(!from && to) {
            // Player entered their craft
            RadarManager.getInstance().startInvisible(p);
        }
    }
}