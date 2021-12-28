package net.countercraft.movecraft.combat.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NameUtils {
    public static String offlineToName(@NotNull OfflinePlayer offlinePlayer) {
        Player p = Bukkit.getPlayer(offlinePlayer.getUniqueId());
        if (p != null)
            return p.getDisplayName() + ChatColor.RESET;

        return offlinePlayer.getName();
    }
}
