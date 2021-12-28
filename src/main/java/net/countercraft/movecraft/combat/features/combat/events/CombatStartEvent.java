package net.countercraft.movecraft.combat.features.combat.events;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class CombatStartEvent extends CombatEvent {
    public CombatStartEvent(@NotNull Player player) {
        super(player);
    }
}