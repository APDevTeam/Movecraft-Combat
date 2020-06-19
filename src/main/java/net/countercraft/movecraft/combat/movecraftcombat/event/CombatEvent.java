package net.countercraft.movecraft.combat.movecraftcombat.event;

import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class CombatEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;

    public CombatEvent(@Nullable Player player) {
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Nullable
    public Player getPlayer() {
        return this.player;
    }

}
