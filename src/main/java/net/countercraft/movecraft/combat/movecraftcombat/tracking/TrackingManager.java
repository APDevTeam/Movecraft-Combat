package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.combat.movecraftcombat.event.CombatReleaseEvent;


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
        if(!damageRecords.containsKey(craft)) {
            return;
        }
        if(damageRecords.get(craft).isEmpty()) {
            damageRecords.remove(craft);
            return;
        }

        HashSet<Player> causes = new HashSet<>();
        long currentTime = System.currentTimeMillis();
        for(DamageRecord r : damageRecords.get(craft)) {
            if(currentTime - r.getTime() < Config.DamageTimeout)
                causes.add(r.getCause());
        }
        if(causes.size() == 0)
            return;
        Bukkit.broadcastMessage(causesToString(craft.getNotificationPlayer(), causes));
        damageRecords.remove(craft);
    }

    public void craftReleased(@NotNull Craft craft) {
        if(!Config.EnableCombatReleaseTracking) {
            damageRecords.remove(craft);
            return;
        }
        if(craft.getNotificationPlayer() == null || craft.getSinking())
            return;
        if(!damageRecords.containsKey(craft))
            return;

        HashSet<DamageRecord> records = damageRecords.get(craft);
        damageRecords.remove(craft);
        long currentTime = System.currentTimeMillis();
        for(DamageRecord r : records) {
            if(currentTime - r.getTime() > Config.DamageTimeout * 1000)
                records.remove(r);
        }
        if(records.size() < 1)
            return;

        CombatReleaseEvent event = new CombatReleaseEvent(craft, craft.getNotificationPlayer());
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled())
            return;

        Player player = craft.getNotificationPlayer();
        Date expiry = new Date(System.currentTimeMillis() + Config.CombatReleaseBanLength * 1000);
        Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), "Combat release", expiry, "Movecraft-Combat AutoBan");
        player.kickPlayer("Combat release!");
    }

    private String causesToString(@NotNull Player sunk, @NotNull HashSet<Player> causes) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sunk.getDisplayName());
        stringBuilder.append(" was sunk by ");
        for(Player p : causes) {
            stringBuilder.append(p.getDisplayName());
            stringBuilder.append(", ");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }
}
