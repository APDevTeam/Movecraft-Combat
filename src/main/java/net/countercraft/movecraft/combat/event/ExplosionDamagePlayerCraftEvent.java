package net.countercraft.movecraft.combat.event;

import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ExplosionDamagePlayerCraftEvent extends CraftEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    @NotNull
    private final Entity damaging;

    public ExplosionDamagePlayerCraftEvent(@NotNull Entity damaging, @NotNull PlayerCraft damaged) {
        super(damaged);
        this.damaging = damaging;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public PlayerCraft getDamaged() {
        return (PlayerCraft) craft;
    }

    @NotNull
    public Entity getDamaging() {
        return damaging;
    }
}
