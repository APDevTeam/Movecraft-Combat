package net.countercraft.movecraft.combat.movecraftcombat.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NameUtils {
    public static String offlineToName(@NotNull OfflinePlayer offlinePlayer) {
        Player p = Bukkit.getPlayer(offlinePlayer.getUniqueId());
        if(p != null)
            return p.getDisplayName();

        return offlinePlayer.getName();
    }
}
