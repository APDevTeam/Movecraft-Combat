package net.countercraft.movecraft.combat.features.directors.events;

import net.countercraft.movecraft.combat.features.directors.Directors;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class CraftDirectEvent extends CraftEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Directors directors;
    private boolean cancelled = false;

    public CraftDirectEvent(Craft craft, Player player, Directors directors) {
        super(craft);
        this.player = player;
        this.directors = directors;
    }

    public Player getPlayer() {
        return player;
    }

    public Directors getDirectors() {
        return directors;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
