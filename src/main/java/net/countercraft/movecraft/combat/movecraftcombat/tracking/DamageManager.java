package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.HashMap;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public class DamageManager extends BukkitRunnable {
    private static DamageManager instance;
    private final HashMap<Craft, HashSet<DamageRecord>> damageRecords = new HashMap<>();

    public DamageManager() {
        instance = this;
        new TNTTracking();
        new FireballTracking();
    }

    public static DamageManager getInstance() {
        return instance;
    }


    public void run() {
        // do all the things
    }


    public void addDamageRecord(@NotNull Craft craft, @NotNull Player cause, @NotNull DamageType type) {
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

    public void craftSunk(@NotNull Craft craft) {
        if(craft.getNotificationPlayer() == null)
            return;
        if(craft.getType().getCruiseOnPilot())
            return;
        if(!damageRecords.containsKey(craft)) {
            return;
        }
        if(damageRecords.get(craft).isEmpty()) {
            damageRecords.remove(craft);
            return;
        }

        HashSet<DamageRecord> causes = new HashSet<>();
        long currentTime = System.currentTimeMillis();
        for(DamageRecord r : damageRecords.get(craft)) {
            if(currentTime - r.getTime() < Config.DamageTimeout * 1000)
                causes.add(r);
        }
        if(causes.size() == 0)
            return;
        Bukkit.broadcastMessage(causesToString(craft.getNotificationPlayer(), causes));
        damageRecords.remove(craft);
    }

    public void craftReleased(@NotNull Craft craft) {
        damageRecords.remove(craft);
    }

    private String causesToString(@NotNull Player sunk, @NotNull HashSet<DamageRecord> causes) {
        DamageRecord latestDamage = null;
        HashSet<Player> players = new HashSet<>();
        for(DamageRecord r : causes) {
            players.add(r.getCause());
            if(latestDamage == null || r.getTime() > latestDamage.getTime())
                latestDamage = r;
        }
        players.remove(latestDamage.getCause());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sunk.getDisplayName());
        stringBuilder.append(" was sunk by ");
        stringBuilder.append(latestDamage.getCause().getDisplayName());
        if(players.size() < 1)
            return stringBuilder.toString();

        stringBuilder.append(" with assists from: ");
        for(Player p : players) {
            stringBuilder.append(p.getDisplayName());
            stringBuilder.append(", ");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }
}
