package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.HashMap;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public class TrackingManager extends BukkitRunnable {
    private static TrackingManager instance;
    private final HashMap<Craft, HashSet<DamageRecord>> damageRecords = new HashMap<>();

    public TrackingManager() {
        instance = this;
        new TNTTracking();
        new FireballTracking();
    }

    public static TrackingManager getInstance() {
        return instance;
    }


    public void run() {
        // do all the things
    }


    public void addRecord(@NotNull Craft craft, @NotNull Player cause, @NotNull DamageType type) {
        if(Config.Debug)
            Bukkit.broadcast((craft.getNotificationPlayer() != null ? craft.getNotificationPlayer().getDisplayName() : "None ") + "'s craft was damaged by a " + type + " by " + cause.getDisplayName(), "movecraft.combat.debug");
        if(damageRecords.containsKey(craft)) {
            HashSet<DamageRecord> craftRecords = damageRecords.get(craft);
            if(craftRecords == null)
                craftRecords = new HashSet<>();
            craftRecords.add(new DamageRecord(cause, type));
        }
        else {
            HashSet<DamageRecord> craftRecords = new HashSet<>();
            craftRecords.add(new DamageRecord(cause, type));
            damageRecords.put(craft, craftRecords);
        }
    }
}
