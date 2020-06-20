package net.countercraft.movecraft.combat.movecraftcombat.listener;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageManager;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;


public class CraftSinkListener implements Listener {
    @EventHandler
    public void sinkListener(CraftSinkEvent e) {
        DamageManager.getInstance().craftSunk(e.getCraft());
        StatusManager.getInstance().craftSunk(e.getCraft());
    }
}
