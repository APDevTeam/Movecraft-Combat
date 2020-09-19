package net.countercraft.movecraft.combat.movecraftcombat.directors;

import com.google.common.collect.HashBiMap;
import net.countercraft.movecraft.craft.Craft;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

}
