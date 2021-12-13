package net.countercraft.movecraft.combat.features.combat.events;

import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class CombatEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }



    private final Player player;


    public CombatEvent(@Nullable Player player) {
        this.player = player;
    }


    @Nullable
    public Player getPlayer() {
        return this.player;
    }
}
