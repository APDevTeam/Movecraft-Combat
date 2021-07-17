package net.countercraft.movecraft.combat.event;

import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;


public class CombatStopEvent extends CombatEvent {
    public CombatStopEvent(@NotNull Player player) {
        super(player);
    }
}