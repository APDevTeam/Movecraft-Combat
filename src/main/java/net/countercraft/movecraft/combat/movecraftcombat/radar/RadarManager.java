package net.countercraft.movecraft.combat.movecraftcombat.radar;

import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;


public class RadarManager extends BukkitRunnable {
    private static RadarManager instance = null;

    // All players who are hidden from pilots
    private final HashSet<Player> invisibles = new HashSet<>();
    // All players who have others hidden
    private final HashSet<Player> pilots = new HashSet<>();


    public static RadarManager getInstance() {
        return instance;
    }


    public void run() {
        // do all the things
    }


    public RadarManager() {
        instance = this;
    }


    public void startInvisible(Player p) {
        if(!Config.EnableAntiRadar || isInvisible(p)) {
            return;
        }

        // Hide player from all pilots
        for(Player pilot : pilots) {
            pilot.hidePlayer(p);
        }

        invisibles.add(p);
    }

    public void endInvisible(Player p) {
        if(!Config.EnableAntiRadar || !isInvisible(p)) {
            return;
        }

        // Show player to all pilots
        for(Player pilot : pilots) {
            pilot.showPlayer(p);
        }

        invisibles.remove(p);
    }

    private boolean isInvisible(Player p) {
        if(!Config.EnableAntiRadar) {
            return false;
        }
        return invisibles.contains(p);
    }

    public void startPilot(Player p) {
        if(!Config.EnableAntiRadar || isPilot(p)) {
            return;
        }

        // Hide all invisible players from player
        for(Player other : invisibles) {
            p.hidePlayer(other);
        }

        pilots.add(p);
    }

    public void endPilot(Player p) {
        if(!Config.EnableAntiRadar || !isPilot(p)) {
            return;
        }

        // Show all invisible players to player
        for(Player other : invisibles) {
            p.showPlayer(other);
        }

        pilots.remove(p);
    }

    public boolean isPilot(Player p) {
        if(!Config.EnableAntiRadar) {
            return false;
        }
        return pilots.contains(p);
    }
}