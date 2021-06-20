package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import net.countercraft.movecraft.combat.movecraftcombat.tracking.damagetype.DamageType;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;


public class DamageRecord {
    private final OfflinePlayer cause;
    private final OfflinePlayer damaged;
    private final DamageType type;
    private final long time;
    private boolean killShot;

    public DamageRecord(@NotNull OfflinePlayer cause, @NotNull OfflinePlayer damaged, @NotNull DamageType type) {
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

    public DamageType getType() {
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
