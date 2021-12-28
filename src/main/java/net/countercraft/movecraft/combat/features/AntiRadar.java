package net.countercraft.movecraft.combat.features;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftScuttleEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class AntiRadar implements Listener {
    public static boolean EnableAntiRadar = false;
    private final Set<Player> invisibles = new HashSet<>();
    private final Set<Player> pilots = new HashSet<>();

    public static void load(@NotNull FileConfiguration config) {
        EnableAntiRadar = config.getBoolean("EnableAntiRadar", false);
    }

    private void startInvisible(Player p) {
        if (invisibles.contains(p))
            return;

        // Hide player from all pilots
        for (Player pilot : pilots)
            pilot.hidePlayer(MovecraftCombat.getInstance(), p);

        invisibles.add(p);
    }

    private void endInvisible(Player p) {
        if (!invisibles.contains(p))
            return;

        // Show player to all pilots
        for (Player pilot : pilots)
            pilot.showPlayer(MovecraftCombat.getInstance(), p);

        invisibles.remove(p);
    }

    private void startPilot(Player p) {
        if (pilots.contains(p))
            return;

        // Hide all invisible players from player
        for (Player other : invisibles)
            p.hidePlayer(MovecraftCombat.getInstance(), other);

        pilots.add(p);
    }

    private void endPilot(Player p) {
        if (!pilots.contains(p))
            return;

        // Show all invisible players to player
        for (Player other : invisibles)
            p.showPlayer(MovecraftCombat.getInstance(), other);

        pilots.remove(p);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftDetect(@NotNull CraftDetectEvent e) {
        if (!EnableAntiRadar)
            return;

        Craft c = e.getCraft();
        if (!(c instanceof PlayerCraft))
            return;

        Player p = ((PlayerCraft) c).getPilot();
        startPilot(p);
        if (MathUtils.locIsNearCraftFast(c, MathUtils.bukkit2MovecraftLoc(p.getLocation())))
            startInvisible(p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftRelease(@NotNull CraftReleaseEvent e) {
        if (!EnableAntiRadar)
            return;

        if (!(e.getCraft() instanceof PlayerCraft))
            return;

        Player p = ((PlayerCraft) e.getCraft()).getPilot();
        endPilot(p);
        endInvisible(p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftSink(@NotNull CraftSinkEvent e) {
        if (!EnableAntiRadar)
            return;

        if (!(e.getCraft() instanceof PlayerCraft))
            return;

        Player p = ((PlayerCraft) e.getCraft()).getPilot();
        endPilot(p);
        endInvisible(p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftScuttle(@NotNull CraftScuttleEvent e) {
        if (!EnableAntiRadar)
            return;

        Player p = e.getCause();
        if (p == null)
            return;

        endPilot(p);
        endInvisible(p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        if (!EnableAntiRadar)
            return;

        Player p = e.getPlayer();

        endPilot(p);
        endInvisible(p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(@NotNull PlayerMoveEvent e) {
        if (!EnableAntiRadar)
            return;

        Player p = e.getPlayer();
        if (!pilots.contains(p))
            return;

        Craft c = CraftManager.getInstance().getCraftByPlayer(p);
        if (c == null)
            return;

        boolean from = MathUtils.locIsNearCraftFast(c, MathUtils.bukkit2MovecraftLoc(e.getFrom()));
        boolean to;
        if (e.getTo() == null)
            to = from;
        else
            to = MathUtils.locIsNearCraftFast(c, MathUtils.bukkit2MovecraftLoc(e.getTo()));

        if (from && !to) // Player left their craft
            endInvisible(p);
        else if (!from && to) // Player entered their craft
            startInvisible(p);
    }
}
