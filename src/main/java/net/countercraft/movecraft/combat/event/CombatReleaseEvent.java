package net.countercraft.movecraft.combat.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.countercraft.movecraft.craft.Craft;


public class CombatReleaseEvent extends CombatEvent implements Cancellable {
    private boolean cancelled = false;
    private final Craft craft;

    public CombatReleaseEvent(@NotNull Craft craft, @Nullable Player player) {
        super(player);
        this.craft = craft;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Craft getCraft() {
        return craft;
    }
}
