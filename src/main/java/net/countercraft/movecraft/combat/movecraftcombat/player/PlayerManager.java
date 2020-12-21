package net.countercraft.movecraft.combat.movecraftcombat.player;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;

public class PlayerManager {
    private final HashMap<Player, PlayerConfig> onlinePlayers = new HashMap<>();

    public void shutDown() {
        for(Player p : onlinePlayers.keySet()) {
            savePlayer(p);
        }
    }

    public void playerQuit(Player player) {
        savePlayer(player);
        onlinePlayers.remove(player);
    }

    public void playerJoin(Player player) {
        loadPlayer(player);
    }

    @Nullable
    public String getSetting(Player player) {
        PlayerConfig config = onlinePlayers.get(player);
        if(config == null)
            return null;

        return config.getSetting();
    }

    public void setSetting(Player player, String setting) {
        PlayerConfig config = onlinePlayers.get(player);
        if(config == null)
            config = new PlayerConfig(player.getUniqueId());

        config.setSetting(setting);
        onlinePlayers.put(player, config);
    }

    @Nullable
    public String getMode(Player player) {
        PlayerConfig config = onlinePlayers.get(player);
        if(config == null)
            return null;

        return config.getMode();
    }

    public void setMode(Player player, String mode) {
        PlayerConfig config = onlinePlayers.get(player);
        if(config == null)
            config = new PlayerConfig(player.getUniqueId());

        config.setMode(mode);
        onlinePlayers.put(player, config);
    }


    private void savePlayer(Player player) {
        PlayerConfig config = onlinePlayers.get(player);
        config.save();
    }

    private void loadPlayer(Player player) {
        PlayerConfig config = new PlayerConfig(player.getUniqueId());
        config.load();
        onlinePlayers.put(player, config);
    }
}
