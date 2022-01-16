package net.countercraft.movecraft.combat.features.tracers.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.UUID;

public class PlayerConfig extends YamlConfiguration {
    private final UUID owner;
    private String setting = "HIGH";
    private String mode = "BLOCKS";


    public PlayerConfig(UUID owner) {
        super();
        this.owner = owner;
    }


    public UUID getOwner() {
        return owner;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
