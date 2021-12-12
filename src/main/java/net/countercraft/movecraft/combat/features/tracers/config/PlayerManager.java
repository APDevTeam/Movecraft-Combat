package net.countercraft.movecraft.combat.features.tracers.config;

import net.countercraft.movecraft.combat.MovecraftCombat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class PlayerManager implements Listener {
    private static PlayerManager instance;

    public static PlayerManager getInstance() {
        return instance;
    }


    private final Map<Player, PlayerConfig> cache = new WeakHashMap<>();

    public PlayerManager() {
        instance = this;
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
        if(config == null)
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPluginDisable(@NotNull PluginDisableEvent e) {
        if(e.getPlugin() != MovecraftCombat.getInstance())
            return;

        for(Player p : cache.keySet()) {
            savePlayer(p);
        }
        cache.clear();
    }
}
