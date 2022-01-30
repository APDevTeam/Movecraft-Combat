package net.countercraft.movecraft.combat.features.tracers.config;

import net.countercraft.movecraft.combat.MovecraftCombat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

public class PlayerConfig {
    private final YamlConfiguration config;
    private final UUID owner;
    private TNTSetting tntSetting = TNTSetting.DEFAULT;
    private TNTMode tntMode = TNTMode.DEFAULT;
    private MovementSetting movementSetting = MovementSetting.DEFAULT;


    public PlayerConfig(UUID owner) {
        File configFile = new File(
                MovecraftCombat.getInstance().getDataFolder().getAbsolutePath() + "/userdata/" + owner + ".yml"
        );
        config = YamlConfiguration.loadConfiguration(configFile);
        this.owner = owner;
    }


    public UUID getOwner() {
        return owner;
    }

    public TNTSetting getTNTSetting() {
        return tntSetting;
    }

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

    public TNTMode getTNTMode() {
        return tntMode;
    }

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

    public MovementSetting getMovementSetting() {
        return movementSetting;
    }

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

    public void load() {
        String temp;

        temp = config.getString(TNTSetting.DATABASE_KEY);
        if(temp != null) {
            switch (temp) {
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
                    tntSetting = TNTSetting.DEFAULT;
                    break;
            }
        }
        else {
            tntSetting = TNTSetting.DEFAULT;
        }

        temp = config.getString(TNTMode.DATABASE_KEY);
        if(temp != null) {
            switch (temp) {
                case "BLOCKS":
                    tntMode = TNTMode.BLOCKS;
                    break;
                case "PARTICLES":
                    tntMode = TNTMode.PARTICLES;
                    break;
                default:
                    tntMode = TNTMode.DEFAULT;
                    break;
            }
        }
        else {
            tntMode = TNTMode.DEFAULT;
        }

        temp = config.getString(MovementSetting.DATABASE_KEY);
        if(temp != null) {
            switch (temp) {
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
                    movementSetting = MovementSetting.DEFAULT;
                    break;
            }
        }
        else {
            movementSetting = MovementSetting.DEFAULT;
        }
    }

    public void save() {
        config.set(TNTSetting.DATABASE_KEY, tntSetting.toString());
        config.set(TNTMode.DATABASE_KEY, tntMode.toString());
        config.set(MovementSetting.DATABASE_KEY, movementSetting.toString());
    }

    public enum TNTSetting {
        OFF,
        LOW,
        MEDIUM,
        HIGH;

        public static final TNTSetting DEFAULT = HIGH;
        public static final String DATABASE_KEY = "setting";

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
                default:
                    return "HIGH";
            }
        }
    }

    public enum TNTMode {
        PARTICLES,
        BLOCKS;

        public static final TNTMode DEFAULT = BLOCKS;
        public static final String DATABASE_KEY = "mode";

        @Contract(pure = true)
        public @NotNull String toString() {
            switch (this) {
                case PARTICLES:
                    return "PARTICLES";
                case BLOCKS:
                    return "BLOCKS";
                default:
                    throw new IllegalStateException("Unknown TNTMode: " + this);
            }
        }
    }

    public enum MovementSetting {
        OFF,
        LOW,
        HIGH;

        public static final MovementSetting DEFAULT = LOW;
        public static final String DATABASE_KEY = "movementSetting";

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
                    throw new IllegalArgumentException("Unknown movement setting: " + this);
            }
        }
    }
}
