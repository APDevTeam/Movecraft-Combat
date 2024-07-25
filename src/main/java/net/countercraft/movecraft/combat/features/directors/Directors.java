package net.countercraft.movecraft.combat.features.directors;

import com.google.common.collect.HashBiMap;
import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.Tags;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class Directors extends BukkitRunnable {
    private static final Set<Directors> instances = new HashSet<>();
    public static Material DirectorTool = null;
    public static Set<Material> Transparent = null;
    private final HashBiMap<PlayerCraft, Player> directors = HashBiMap.create();


    public Directors() {
        instances.add(this);
    }

    public static void load(@NotNull FileConfiguration config) {
        Object tool = config.get("DirectorTool");
        Material directorTool = null;
        if (tool instanceof String)
            directorTool = Material.getMaterial((String) tool);
        if (directorTool == null)
            MovecraftCombat.getInstance().getLogger().severe("Failed to load director tool " + ((tool == null) ? "null" : tool.toString()));
        else
            DirectorTool = directorTool;

        if (!config.contains("TransparentBlocks")) {
            Transparent = new HashSet<>();
            return;
        }
        var transparent = config.getList("TransparentBlocks");
        if (transparent == null)
            throw new IllegalStateException();

        Transparent = new HashSet<>();
        for (Object o : transparent) {
            if (o instanceof String) {
                var tagged = Tags.parseMaterials((String) o);
                Transparent.addAll(tagged);
            }
            else {
                MovecraftCombat.getInstance().getLogger().severe("Failed to load transparent " + o.toString());
            }
        }
    }

    @Override
    public void run() {

    }


    public void addDirector(@NotNull PlayerCraft craft, @NotNull Player player) {
        if (directors.containsValue(player))
            directors.inverse().remove(player);

        directors.put(craft, player);
    }

    public boolean isDirector(@NotNull Player player) {
        return directors.containsValue(player);
    }

    public boolean hasDirector(@NotNull PlayerCraft craft) {
        if (!directors.containsKey(craft))
            return false;

        Player director = directors.get(craft);
        return director != null && director.isOnline();
    }

    public void removeDirector(@NotNull Player player) {
        directors.inverse().remove(player);
    }

    public static void clearDirector(@NotNull Player player) {
        for (var instance : instances)
            instance.removeDirector(player);
    }

    public static boolean isAnyDirector(@NotNull Player player) {
        for (var instance : instances) {
            if (instance.isDirector(player))
                return true;
        }
        return false;
    }

    @Nullable
    public Player getDirector(@NotNull PlayerCraft craft) {
        Player director = directors.get(craft);
        if (director == null || !director.isOnline())
            return null;

        return director;
    }
}
