package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.ArrayList;
import java.util.HashMap;

import net.countercraft.movecraft.combat.movecraftcombat.event.CraftDamagedByEvent;
import net.countercraft.movecraft.combat.movecraftcombat.event.CraftSunkByEvent;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.damagetype.DamageType;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public class DamageManager extends BukkitRunnable {
    private static DamageManager instance;
    private final HashMap<Craft, ArrayList<DamageRecord>> damageRecords = new HashMap<>();

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

        DamageRecord damageRecord = new DamageRecord(cause, type);
        Bukkit.getServer().getPluginManager().callEvent(new CraftDamagedByEvent(craft, damageRecord));

        if(damageRecords.containsKey(craft) && damageRecords.get(craft) != null) {
            ArrayList<DamageRecord> craftRecords = damageRecords.get(craft);
            craftRecords.add(damageRecord);
        }
        else {
            ArrayList<DamageRecord> craftRecords = new ArrayList<>();
            craftRecords.add(damageRecord);
            damageRecords.put(craft, craftRecords);
        }
    }

    public void craftSunk(@NotNull Craft craft) {
        if(craft.getNotificationPlayer() == null)
            return;
        if(craft.getType().getCruiseOnPilot())
            return;
        if(!damageRecords.containsKey(craft))
            return;
        ArrayList<DamageRecord> records = damageRecords.get(craft);
        if(records.isEmpty()) {
            damageRecords.remove(craft);
            return;
        }

        // Set last damage record as kill shot
        records.get(records.size() - 1).setKillShot(true);

        CraftSunkByEvent e = new CraftSunkByEvent(craft, records);
        Bukkit.getServer().getPluginManager().callEvent(e);
        Bukkit.broadcastMessage(e.causesToString());
        damageRecords.remove(craft);

    }

    public void craftReleased(@NotNull Craft craft) {
        damageRecords.remove(craft);
    }
}
