package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import net.countercraft.movecraft.combat.movecraftcombat.tracking.damagetype.DamageType;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;


public class DamageRecord {
    private final Player cause;
    private final DamageType type;
    private final long time;

    public DamageRecord(@NotNull Player cause, @NotNull DamageType type) {
        this.cause = cause;
        this.type = type;
        time = System.currentTimeMillis();
    }

    public Player getCause() {
        return cause;
    }

    public DamageType getType() {
        return type;
    }

    public long getTime() {
        return time;
    }
}
