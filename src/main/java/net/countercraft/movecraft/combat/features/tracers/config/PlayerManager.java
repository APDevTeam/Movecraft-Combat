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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class PlayerManager implements Listener {
    private final Map<Player, PlayerConfig> cache = new WeakHashMap<>();

    @NotNull
    public PlayerConfig.TNTSetting getTNTSetting(Player player) {
        var config = cache.get(player);
        if (config == null)
            return PlayerConfig.TNTSetting.DEFAULT;

        return config.getTNTSetting();
    }

    public void setTNTSetting(Player player, String setting) {
        var config = cache.get(player);
        if (config == null) {
            config = loadPlayer(player);
            cache.put(player, config);
        }

        config.setTNTSetting(setting);
    }

    @NotNull
    public PlayerConfig.TNTMode getTNTMode(Player player) {
        var config = cache.get(player);
        if (config == null)
            return PlayerConfig.TNTMode.DEFAULT;

        return config.getTNTMode();
    }

    public void setTNTMode(Player player, String mode) {
        var config = cache.get(player);
        if (config == null) {
            config = loadPlayer(player);
            cache.put(player, config);
        }

        config.setTNTMode(mode);
    }

    @NotNull
    public PlayerConfig.MovementSetting getMovementSetting(Player player) {
        var config = cache.get(player);
        if (config == null)
            return PlayerConfig.MovementSetting.DEFAULT;

        return config.getMovementSetting();
    }

    public void setMovementSetting(Player player, String setting) {
        var config = cache.get(player);
        if (config == null) {
            config = loadPlayer(player);
            cache.put(player, config);
        }

        config.setMovementSetting(setting);
    }

    private void savePlayer(Player player) {
        var config = cache.get(player);
        if (config == null)
            return;

        Gson gson = buildGson();
        String str = null;
        try {
            str = gson.toJson(config);
        } catch (JsonIOException e) {
            e.printStackTrace();
            return;
        }

        File file = getFile(player.getUniqueId());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @NotNull
    private PlayerConfig loadPlayer(@NotNull Player player) {
        File file = getFile(player.getUniqueId());
        if (!file.exists() || !file.isFile() || !file.canRead())
            return new PlayerConfig(player.getUniqueId());

        Gson gson = buildGson();
        PlayerConfig config = null;
        try {
            config = gson.fromJson(new FileReader(file), new TypeToken<PlayerConfig>() {
            }.getType());
        } catch (FileNotFoundException ignored) {
            return new PlayerConfig(player.getUniqueId());
        } catch (JsonSyntaxException | JsonIOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private File getFile(UUID owner) {
        return new File(
                MovecraftCombat.getInstance().getDataFolder().getAbsolutePath() + "/userdata/" + owner + ".json");
    }

    private static Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.serializeNulls();
        return builder.create();
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
        if (e.getPlugin() != MovecraftCombat.getInstance())
            return;

        for (Player p : cache.keySet()) {
            savePlayer(p);
        }
        cache.clear();
    }
}
