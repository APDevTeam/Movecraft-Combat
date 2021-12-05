package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.combat.tracking.DamageManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class CraftReleaseListener implements Listener {
    @EventHandler
    public void releaseListener(CraftReleaseEvent e) {
        StatusManager.getInstance().craftReleased(e);
        if(!(e.getCraft() instanceof PlayerCraft))
            return;

        PlayerCraft playerCraft = (PlayerCraft) e.getCraft();
        DamageManager.getInstance().craftReleased(playerCraft);
    }
}
