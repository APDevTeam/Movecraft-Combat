package net.countercraft.movecraft.combat.event;

import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftCollisionExplosionEvent;
import net.countercraft.movecraft.events.CraftEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link CraftCollisionExplosionEvent} which damages a {@link PlayerCraft}.
 */
public class CollisionDamagePlayerCraftEvent extends CraftEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    @NotNull
    private final PlayerCraft damaged;

    public CollisionDamagePlayerCraftEvent(@NotNull PilotedCraft damaging, @NotNull PlayerCraft damaged) {
        super(damaging);
        this.damaged = damaged;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public PlayerCraft getDamaged() {
        return damaged;
    }

    @NotNull
    public PilotedCraft getDamaging() {
        return (PilotedCraft) craft;
    }
}
