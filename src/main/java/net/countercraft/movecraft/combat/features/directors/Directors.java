package net.countercraft.movecraft.combat.features.directors;

import com.google.common.collect.HashBiMap;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.Tags;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    public DirectorData addDirector(Player player, PlayerCraft craft, String line1, String line2, String line3) {
        if (directors.containsValue(player)) directors.remove(player);
        List<String> selectedLines = new ArrayList<>();
        selectedLines.add(line1);
        selectedLines.add(line2);
        selectedLines.add(line3);
        if (line1.isBlank() && line2.isBlank() && line3.isBlank()) selectedLines.clear();
        DirectorData data = new DirectorData(player, craft, selectedLines);
        directors.put(player, data);
        System.out.println("New director added: " + directors.get(player).getSelectedSigns());
        System.out.println("Director name: " + directors.get(player).getPlayer());
        return data;
    }

    public HashSet<DirectorData> getDirectorDataSet(PlayerCraft craft) {
        HashSet<DirectorData> directorDataSet = new HashSet<>();
        for (DirectorData data : directors.values()) {
            if (data.getCraft() == craft) directorDataSet.add(data);
        }
        return directorDataSet;
    }

    public boolean hasDirector(PlayerCraft craft) {
        if (craft == null) return false;
        for (DirectorData data : directors.values()) {
            if (data.getCraft() == craft) return true;
        }
        return false;
    }

    public boolean isNodesShared(DirectorData director) {
        PlayerCraft craft = director.getCraft();
        List<String> selectedSigns = director.getSelectedSigns();
        return directors.values().stream()
                .filter(data -> data != director && data.getCraft() == craft)
                .anyMatch(data -> data.getSelectedSigns().stream().anyMatch(selectedSigns::contains));
    }

    public boolean isDirector(@NotNull Player player) {
        return directors.containsKey(player);
    }

    public void removeDirector(@NotNull Player player) {
        directors.remove(player);
    }

    public void clearDirector(@NotNull Player player) {
        for (var instance : instances)
            instance.removeDirector(player);
    }

    public HashSet<Location> getLocations(DirectorData data) {
        if (data.getSelectedSigns().isEmpty() || data.getCraft() == null) {
            return null;
        }
        PlayerCraft craft = data.getCraft();

        HashSet<Location> locations = new HashSet<>();
        for (MovecraftLocation location : craft.getHitBox()) {
            Block block = craft.getWorld().getBlockAt(location.getX(), location.getY(), location.getZ());
            if (!(block.getState() instanceof Sign))
                continue;

            Sign sign = (Sign) block.getState();

            if (!sign.getLine(0).equalsIgnoreCase("subcraft rotate")) {
                continue;
            }
            if (sign.getLine(3).isBlank()) {
                System.out.println("Sign is blank");
                continue;
            }
            if (data.getSelectedSigns().contains(sign.getLine(3))) {
                System.out.println("Sign found.");
                locations.add(block.getLocation());
            }
        }
        return locations;
    }
}
