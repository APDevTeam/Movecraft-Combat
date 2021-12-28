package net.countercraft.movecraft.combat.features.tracking.events;

import net.countercraft.movecraft.combat.features.tracking.types.Type;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CraftFireWeaponEvent extends CraftEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    @NotNull
    private final Type weaponType;

    public CraftFireWeaponEvent(@NotNull Craft craft, @NotNull Type weaponType) {
        super(craft);
        this.weaponType = weaponType;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public Type getWeaponType() {
        return weaponType;
    }
}
