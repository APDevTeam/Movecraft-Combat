package net.countercraft.movecraft.combat.features.damagetracking;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.features.damagetracking.events.CraftDamagedByEvent;
import net.countercraft.movecraft.combat.features.damagetracking.events.CraftReleasedByEvent;
import net.countercraft.movecraft.combat.features.damagetracking.events.CraftSunkByEvent;
import net.countercraft.movecraft.combat.features.damagetracking.types.DamageType;
import net.countercraft.movecraft.combat.features.damagetracking.types.TorpedoDamage;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CraftStatus;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftCollisionExplosionEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamageManager implements Listener {
    @Deprecated(forRemoval = true)
    private static DamageManager instance;

    @Nullable @Deprecated(forRemoval = true)
    public static DamageManager getInstance() {
        return instance;
    }

    private final Map<PlayerCraft, List<DamageRecord>> damageRecords = new HashMap<>();

    public DamageManager() {
        instance = this;
    }

    public void addDamageRecord(@NotNull PlayerCraft craft, @NotNull Player cause, @NotNull DamageType type) {
        DamageRecord damageRecord = new DamageRecord(craft.getPilot(), cause, type);
        Bukkit.getServer().getPluginManager().callEvent(new CraftDamagedByEvent(craft, damageRecord));

        if(damageRecords.containsKey(craft)) {
            List<DamageRecord> records = damageRecords.get(craft);
            records.add(damageRecord);
        }
        else {
            List<DamageRecord> records = new ArrayList<>();
            records.add(damageRecord);
            damageRecords.put(craft, records);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftRelease(@NotNull CraftReleaseEvent e) {
        if(!(e.getCraft() instanceof PlayerCraft))
            return;

        PlayerCraft craft = (PlayerCraft) e.getCraft();
        if(!damageRecords.containsKey(craft))
            return;

        List<DamageRecord> records = damageRecords.get(craft);
        if(records.isEmpty()) {
            damageRecords.remove(craft);
            return;
        }

        // Call event
        CraftReleasedByEvent event = new CraftReleasedByEvent(craft, records);
        Bukkit.getServer().getPluginManager().callEvent(event);

        damageRecords.remove(craft);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftSink(@NotNull CraftSinkEvent e) {
        if(!(e.getCraft() instanceof PlayerCraft))
            return;

        PlayerCraft craft = (PlayerCraft) e.getCraft();
        if(!damageRecords.containsKey(craft))
            return;

        List<DamageRecord> records = damageRecords.get(craft);
        if(records.isEmpty()) {
            damageRecords.remove(craft);
            return;
        }

        // Set last damage record as kill shot
        records.get(records.size() - 1).setKillShot(true);

        // Call event
        CraftSunkByEvent event = new CraftSunkByEvent(craft, records);
        Bukkit.getServer().getPluginManager().callEvent(event);

        Bukkit.broadcastMessage(event.causesToString());
        damageRecords.remove(craft);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCollisionExplosion(@NotNull CraftCollisionExplosionEvent e) {
        if(!Config.EnableTorpedoTracking)
            return;
        if(!(e.getCraft() instanceof PilotedCraft))
            return;

        PilotedCraft damaging = (PilotedCraft) e.getCraft();

        //check if the craft should sink
        CraftStatus status = Movecraft.getInstance().getAsyncManager().checkCraftStatus(e.getCraft());
        if(status.isSinking()) {
            e.setCancelled(true);
            e.getCraft().setCruising(false);
            e.getCraft().sink();
        }

        PlayerCraft damaged = fastNearestPlayerCraftToCraft(damaging);
        if(damaged == null)
            return;
        if(!MathUtils.locIsNearCraftFast(damaged, MathUtils.bukkit2MovecraftLoc(e.getLocation())))
            return;

        addDamageRecord(damaged, damaging.getPilot(), new TorpedoDamage());
    }

    @Nullable
    private PlayerCraft fastNearestPlayerCraftToCraft(@NotNull Craft source) {
        MovecraftLocation loc = source.getHitBox().getMidPoint();
        PlayerCraft closest = null;
        long closestDistSquared = Long.MAX_VALUE;
        for(Craft other : CraftManager.getInstance()) {
            if(other == source)
                continue;
            if(other.getWorld() != source.getWorld())
                continue;
            if(!(other instanceof PlayerCraft))
                continue;

            long distSquared = other.getHitBox().getMidPoint().distanceSquared(loc);
            if(distSquared < closestDistSquared) {
                closestDistSquared = distSquared;
                closest = (PlayerCraft) other;
            }
        }
        return closest;
    }
}
