package net.countercraft.movecraft.combat.features.directors;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.Tags;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Directors extends BukkitRunnable {
    private static final Set<Directors> instances = new HashSet<>();
    public static Material DirectorTool = null;
    public static Set<Material> Transparent = null;
    private Map<Player, DirectorData> directors = new HashMap<>();

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

    public void addDirector(Player player, PlayerCraft craft, Set<String> selectedLines) {
        if (directors.containsValue(player)) directors.remove(player);
        DirectorData data = new DirectorData(player, craft, selectedLines);
        directors.put(player, data);
    }

    @NotNull
    public HashSet<DirectorData> getCraftDirectors(@NotNull PlayerCraft craft) {
        HashSet<DirectorData> directorDataSet = new HashSet<>();
        for (DirectorData directorData : directors.values()) {
            if (directorData.getCraft() == craft) directorDataSet.add(directorData);
        }
        return directorDataSet;
    }


    public boolean hasDirector(@Nullable PlayerCraft craft) {
        if (craft == null) return false;
        for (DirectorData data : directors.values()) {
            if (data.getCraft() == craft) return true;
        }
        return false;
    }

    //This ensures that no two director nodes are shared between the director players.
    public boolean isNodesShared(Set<String> selectedStrings, PlayerCraft craft, Player player) {
        for (DirectorData directorData : getCraftDirectors(craft)) {
            if (directorData.getPlayer() == player) continue;
            if (selectedStrings.isEmpty()) return false;
            Set<String> stringsCopy = new HashSet<>(selectedStrings);
            stringsCopy.retainAll(directorData.getSelectedNodes());
            if (!stringsCopy.isEmpty()) return true;
        }
        return false;
    }

    @NotNull
    public Set<String> processSign(Sign sign) {
        String[] lines = sign.getLines();
        Set<String> selectedLines = new HashSet<>();

        for (int i = 1; i < lines.length ; i++) {
            String line = lines[i].trim();
            if (!line.isBlank()) selectedLines.add(line);
        }

        return selectedLines;
    }

    @Nullable
    public Player getClosestDirectorFromProjectile(
            Set<DirectorData> directorDataSet,
            Vector projectile,
            int nodeDistance
    ) {
        for (DirectorData directorData : directorDataSet) {
            for (Vector signLocation : directorData.getSignLocations()) {
                // Calculate squared distance.
                if (signLocation.distanceSquared(projectile) <= (nodeDistance * nodeDistance)) {
                    return directorData.getPlayer();
                }
            }
        }
        return null;
    }

    public boolean isDirector(@NotNull Player player) {
        return directors.containsKey(player);
    }

    public void removeDirector(@NotNull Player player) {
        directors.remove(player);
    }

    //This clears all DirectorData that might be already assigned to the player.
    public void clearDirector(@NotNull Player player) {
        for (var instance : instances)
            instance.removeDirector(player);
    }
}
