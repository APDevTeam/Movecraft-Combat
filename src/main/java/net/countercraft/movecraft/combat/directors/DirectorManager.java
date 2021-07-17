package net.countercraft.movecraft.combat.directors;

import com.google.common.collect.HashBiMap;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DirectorManager extends BukkitRunnable {
    private final HashBiMap<PlayerCraft, Player> directors = HashBiMap.create();

    public void run() {

    }


    public void addDirector(@NotNull PlayerCraft craft, @NotNull Player player) {
        if(directors.containsValue(player)) {
            directors.inverse().remove(player);
        }
        directors.put(craft, player);
    }

    public boolean isDirector(@NotNull Player player) {
        return directors.containsValue(player);
    }

    public boolean hasDirector(@NotNull PlayerCraft craft) {
        if(!directors.containsKey(craft))
            return false;

        Player director = directors.get(craft);
        return director != null && director.isOnline();
    }

    public void removeDirector(@NotNull Player player) {
        directors.inverse().remove(player);
    }

    @Nullable
    public Player getDirector(@NotNull PlayerCraft craft) {
        Player director = directors.get(craft);
        if(director == null || !director.isOnline())
            return null;

        return director;
    }

}
