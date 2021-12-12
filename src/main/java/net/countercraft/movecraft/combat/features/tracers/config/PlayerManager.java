package net.countercraft.movecraft.combat.features.tracers.config;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class PlayerManager implements Listener {
    private final Map<Player, PlayerConfig> cache = new WeakHashMap<>();

    public void shutDown() {
        for(Player p : cache.keySet()) {
            savePlayer(p);
        }
    }

    @Nullable
    public String getSetting(Player player) {
        var config = cache.get(player);
        if(config == null)
            return null;

        return config.getSetting();
    }

    public void setSetting(Player player, String setting) {
        var config = cache.get(player);
        if(config == null) {
            config = new PlayerConfig(player.getUniqueId());
            config.load();
        }

        config.setSetting(setting);
        cache.put(player, config);
    }

    @Nullable
    public String getMode(Player player) {
        var config = cache.get(player);
        if(config == null)
            return null;

        return config.getMode();
    }

    public void setMode(Player player, String mode) {
        var config = cache.get(player);
        if(config == null) {
            config = new PlayerConfig(player.getUniqueId());
            config.load();
        }

        config.setMode(mode);
        cache.put(player, config);
    }


    private void savePlayer(Player player) {
        var config = cache.get(player);
        if(config == null) // TODO: Not sure the side effects of this
            return;

        config.save();
    }

    @NotNull
    private PlayerConfig loadPlayer(@NotNull Player player) {
        var config = new PlayerConfig(player.getUniqueId());
        config.load();
        return config;
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();
        var config = loadPlayer(player);
        cache.put(player, config);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();
        savePlayer(player);
        cache.remove(player);
    }
}
