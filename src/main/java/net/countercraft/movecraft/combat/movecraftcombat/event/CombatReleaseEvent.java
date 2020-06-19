package net.countercraft.movecraft.combat.movecraftcombat.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.event.HandlerList;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftEvent;


public class CombatReleaseEvent extends CraftEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private Player player = null;

    public CombatReleaseEvent(@NotNull Craft craft, @Nullable Player player) {
        super(craft);
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

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Nullable
    public Player getPlayer() {
        return this.player;
    }
}
