package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;


public class DamageRecord {
    private Player cause;
    private DamageType type;
    private long time;

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
