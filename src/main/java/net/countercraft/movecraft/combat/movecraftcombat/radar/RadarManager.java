package net.countercraft.movecraft.combat.movecraftcombat.radar;

import org.bukkit.entity.Player;

import java.util.HashSet;


public class RadarManager {
    private static RadarManager instance = null;

    // All players who are hidden from pilots
    private HashSet<Player> invisibles;
    // All players who have others hidden
    private HashSet<Player> pilots;


    public static RadarManager getInstance() {
        return instance;
    }


    public RadarManager() {
        instance = this;
        invisibles = new HashSet<>();
        pilots = new HashSet<>();
    }


    public void startInvisible(Player p) {
        if(isInvisible(p)) {
            return;
        }

        // Hide player from all pilots
        for(Player pilot : pilots) {
            pilot.hidePlayer(p);
        }

        invisibles.add(p);
    }

    public void endInvisible(Player p) {
        if(!isInvisible(p)) {
            return;
        }

        // Show player to all pilots
        for(Player pilot : pilots) {
            pilot.showPlayer(p);
        }

        invisibles.remove(p);
    }

    public boolean isInvisible(Player p) {
        return invisibles.contains(p);
    }

    public void startPilot(Player p) {
        if(isPilot(p)) {
            return;
        }

        // Hide all invisible players from player
        for(Player other : invisibles) {
            p.hidePlayer(other);
        }

        pilots.add(p);
    }

    public void endPilot(Player p) {
        if(!isPilot(p)) {
            return;
        }

        // Show all invisible players to player
        for(Player other : invisibles) {
            p.showPlayer(other);
        }

        pilots.remove(p);
    }

    public boolean isPilot(Player p) {
        return pilots.contains(p);
    }
}