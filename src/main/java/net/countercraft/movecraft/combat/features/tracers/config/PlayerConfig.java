package net.countercraft.movecraft.combat.features.tracers.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerConfig {
    @Nullable
    private UUID owner = null;
    @NotNull
    private TNTSetting tntSetting = TNTSetting.DEFAULT;
    @NotNull
    private TNTMode tntMode = TNTMode.DEFAULT;
    @NotNull
    private MovementSetting movementSetting = MovementSetting.DEFAULT;

    public PlayerConfig() {
    }

    public PlayerConfig(UUID owner) {
        this.owner = owner;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    @NotNull
    public TNTSetting getTNTSetting() {
        return tntSetting;
    }

    @NotNull
    public void setTNTSetting(@NotNull String setting) throws IllegalArgumentException {
        switch (setting) {
            case "OFF":
                tntSetting = TNTSetting.OFF;
                break;
            case "LOW":
                tntSetting = TNTSetting.LOW;
                break;
            case "MEDIUM":
                tntSetting = TNTSetting.MEDIUM;
                break;
            case "HIGH":
                tntSetting = TNTSetting.HIGH;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @NotNull
    public TNTMode getTNTMode() {
        return tntMode;
    }

    @NotNull
    public void setTNTMode(@NotNull String mode) throws IllegalArgumentException {
        switch (mode) {
            case "BLOCKS":
                tntMode = TNTMode.BLOCKS;
                break;
            case "PARTICLES":
                tntMode = TNTMode.PARTICLES;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @NotNull
    public MovementSetting getMovementSetting() {
        return movementSetting;
    }

    @NotNull
    public void setMovementSetting(@NotNull String setting) throws IllegalArgumentException {
        switch (setting) {
            case "OFF":
                movementSetting = MovementSetting.OFF;
                break;
            case "LOW":
                movementSetting = MovementSetting.LOW;
                break;
            case "HIGH":
                movementSetting = MovementSetting.HIGH;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public enum TNTSetting {
        OFF,
        LOW,
        MEDIUM,
        HIGH;

        public static final TNTSetting DEFAULT = HIGH;

        @Contract(pure = true)
        public @NotNull String toString() {
            switch (this) {
                case OFF:
                    return "OFF";
                case LOW:
                    return "LOW";
                case MEDIUM:
                    return "MEDIUM";
                case HIGH:
                    return "HIGH";
                default:
                    return DEFAULT.toString();
            }
        }
    }

    public enum TNTMode {
        PARTICLES,
        BLOCKS;

        public static final TNTMode DEFAULT = BLOCKS;

        @Contract(pure = true)
        public @NotNull String toString() {
            switch (this) {
                case PARTICLES:
                    return "PARTICLES";
                case BLOCKS:
                    return "BLOCKS";
                default:
                    return DEFAULT.toString();
            }
        }
    }

    public enum MovementSetting {
        OFF,
        LOW,
        HIGH;

        public static final MovementSetting DEFAULT = LOW;

        @Contract(pure = true)
        public @NotNull String toString() {
            switch (this) {
                case OFF:
                    return "OFF";
                case LOW:
                    return "LOW";
                case HIGH:
                    return "HIGH";
                default:
                    return DEFAULT.toString();
            }
        }
    }
}
