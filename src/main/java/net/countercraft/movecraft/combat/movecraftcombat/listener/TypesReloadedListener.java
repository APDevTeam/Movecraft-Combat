package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.events.TypesReloadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TypesReloadedListener implements Listener {
    @EventHandler
    public void typesReloadedListener(TypesReloadedEvent e) {
        MovecraftCombat.getInstance().reloadTypes();
    }
}
