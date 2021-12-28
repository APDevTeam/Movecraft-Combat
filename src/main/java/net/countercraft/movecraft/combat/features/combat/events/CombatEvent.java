package net.countercraft.movecraft.combat.features.combat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;


public class CombatEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;

    public CombatEvent(@Nullable Player player) {
        this.player = player;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Nullable
    public Player getPlayer() {
        return this.player;
    }
}
