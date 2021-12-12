package net.countercraft.movecraft.combat.features.tracking;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.event.ExplosionDamagePlayerCraftEvent;
import net.countercraft.movecraft.combat.features.directors.CannonDirectors;
import net.countercraft.movecraft.combat.features.combat.CombatRelease;
import net.countercraft.movecraft.combat.features.tracking.types.TNTCannonDamage;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TNTTracking implements Listener {
    @Nullable @Deprecated(forRemoval = true)
    private static TNTTracking instance = null;

    @Nullable @Deprecated(forRemoval = true)
    public static TNTTracking getInstance() {
        return instance;
    }


    @NotNull
    private final DamageTracking manager;

    public TNTTracking(@NotNull DamageTracking manager) {
        this.manager = manager;
        instance = this;
    }

    public void dispensedTNT(@NotNull PlayerCraft craft, @NotNull TNTPrimed tnt) {
        Player sender;
        if(CannonDirectors.getInstance().hasDirector(craft))
            sender = CannonDirectors.getInstance().getDirector(craft);
        else
            sender = craft.getPilot();
        if(sender == null)
            return;

        tnt.setMetadata("MCC-Sender", new FixedMetadataValue(MovecraftCombat.getInstance(), sender.getUniqueId().toString()));
    }

    public void damagedCraft(@NotNull PlayerCraft craft, @NotNull TNTPrimed tnt) {
        List<MetadataValue> meta = tnt.getMetadata("MCC-Sender");
        if(meta.isEmpty())
            return;

        UUID sender = UUID.fromString(meta.get(0).asString());
        Player cause = MovecraftCombat.getInstance().getServer().getPlayer(sender);
        if(cause == null || !cause.isOnline())
            return;

        manager.addDamageRecord(craft, cause, new TNTCannonDamage());
        CombatRelease.getInstance().registerEvent(craft.getPilot());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosionDamagePlayerCraft(@NotNull ExplosionDamagePlayerCraftEvent e) {
        if(!DamageTracking.EnableTNTTracking)
            return;
        if(!(e.getDamaging() instanceof TNTPrimed))
            return;

        damagedCraft((PlayerCraft) e.getCraft(), (TNTPrimed) e.getDamaging());
    }
}
