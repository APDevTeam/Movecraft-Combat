package net.countercraft.movecraft.combat.features.directors;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Determines when a player swaps an elytra on themselves
 */
public class DirectorElytraListener extends Directors implements Listener {
    private static boolean DisableDirectorElytra = false;

    public static void load(@NotNull FileConfiguration config) {
        DisableDirectorElytra = config.getBoolean("DisableDirectorElytra", false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void ElytraEquipEvent(@NotNull PlayerArmorChangeEvent e) {
        Player p = e.getPlayer();

        if (!DisableDirectorElytra)
            return;

        // If the player equips an elytra, remove their directors
        if (e.getNewItem().getType().equals(Material.ELYTRA)) {
            clearDirector(p);
            p.sendMessage(I18nSupport.getInternationalisedString("Director - No Elytra While Directing"));
        }
    }

}