package net.countercraft.movecraft.combat.features.tracking.events;

import net.countercraft.movecraft.combat.features.tracking.DamageRecord;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CraftDamagedByEvent extends CraftEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final DamageRecord damageRecord;

    public CraftDamagedByEvent(@NotNull Craft craft, @NotNull DamageRecord damageRecord) {
        super(craft);
        this.damageRecord = damageRecord;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public DamageRecord getDamageRecord() {
        return this.damageRecord;
    }
}
