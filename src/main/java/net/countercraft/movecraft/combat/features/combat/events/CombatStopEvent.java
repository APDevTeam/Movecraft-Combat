package net.countercraft.movecraft.combat.features.combat.events;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class CombatStopEvent extends CombatEvent {
    public CombatStopEvent(@NotNull Player player) {
        super(player);
    }
}