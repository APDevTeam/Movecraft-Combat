package net.countercraft.movecraft.combat.features.tracking;

import net.countercraft.movecraft.combat.features.tracking.types.Type;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;


public class DamageRecord {
    private final OfflinePlayer cause;
    private final OfflinePlayer damaged;
    private final Type type;
    private final long time;
    private boolean killShot;

    public DamageRecord(@NotNull OfflinePlayer cause, @NotNull OfflinePlayer damaged, @NotNull Type type) {
        this.cause = cause;
        this.damaged = damaged;
        this.type = type;
        time = System.currentTimeMillis();
        killShot = false;
    }

    public OfflinePlayer getCause() {
        return cause;
    }

    public OfflinePlayer getDamaged() {
        return damaged;
    }

    public Type getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    public boolean isKillShot() {
        return killShot;
    }

    public void setKillShot(boolean killShot) {
        this.killShot = killShot;
    }
}
