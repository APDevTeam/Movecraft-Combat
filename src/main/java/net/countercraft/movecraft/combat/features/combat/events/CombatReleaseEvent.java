package net.countercraft.movecraft.combat.features.combat.events;

import net.countercraft.movecraft.craft.Craft;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CombatReleaseEvent extends CombatEvent implements Cancellable {
    private final Craft craft;
    private boolean cancelled = false;

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
