package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.HashMap;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.countercraft.movecraft.craft.Craft;


public class TrackingManager extends BukkitRunnable {
    private static TrackingManager instance;
    private TNTTracking tntTracking;
    private final HashMap<Craft, HashSet<DamageRecord>> damageRecords = new HashMap<>();

    public TrackingManager() {
        instance = this;
        tntTracking = new TNTTracking();
    }

    public static TrackingManager getInstance() {
        return instance;
    }


    public void run() {
        // do all the things
    }


    public void addRecord(@NotNull Craft craft, @NotNull Player cause, @NotNull DamageType type) {
        if(damageRecords.containsKey(craft)) {
            HashSet<DamageRecord> craftRecords = damageRecords.get(craft);
            if(craftRecords == null) {
                craftRecords = new HashSet<>();
            }
            craftRecords.add(new DamageRecord(cause, type));
        }
        else {
            HashSet<DamageRecord> craftRecords = new HashSet<>();
            craftRecords.add(new DamageRecord(cause, type));
            damageRecords.put(craft, craftRecords);
        }
    }
}
