package net.countercraft.movecraft.combat.movecraftcombat.directors;

import com.google.common.collect.HashBiMap;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DirectorManager extends BukkitRunnable {
    private final HashBiMap<Craft, Player> directors = HashBiMap.create();

    public void run() {

    }


    public void addDirector(@NotNull Craft craft, @NotNull Player player) {
        if(directors.containsValue(player)) {
            directors.inverse().remove(player);
        }
        directors.put(craft, player);
    }

    public boolean isDirector(@NotNull Player player) {
        return directors.containsValue(player);
    }

    public boolean hasDirector(@NotNull Craft craft) {
        if(!directors.containsKey(craft))
            return false;

        Player director = directors.get(craft);
        return director != null && director.isOnline();
    }

    public void removeDirector(@NotNull Player player) {
        directors.inverse().remove(player);
    }

    @Nullable
    public Player getDirector(@NotNull Craft craft) {
        Player director = directors.get(craft);
        if(director == null || !director.isOnline())
            return null;

        return director;
    }

    protected Craft getDirectingCraft(Metadatable directed) {
        if(!Config.EnableTNTTracking)
            return null;

        List<MetadataValue> meta = directed.getMetadata("MCC-Sender");
        if(meta.isEmpty())
            return null;

        Player sender = Bukkit.getPlayer(UUID.fromString(meta.get(0).asString()));
        if (sender == null || !sender.isOnline())
            return null;

        Craft c = CraftManager.getInstance().getCraftByPlayer(sender);
        if (c == null || c.getSinking())
            return null;
        return c;
    }
}
