package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.events.TypesReloadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TypesReloadedListener implements Listener {
    @EventHandler
    public void typesReloadedListener(TypesReloadedEvent e) {
        MovecraftCombat.getInstance().reloadTypes();
    }
}
