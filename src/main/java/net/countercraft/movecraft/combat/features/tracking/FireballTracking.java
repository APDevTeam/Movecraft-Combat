package net.countercraft.movecraft.combat.features.tracking;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.event.ExplosionDamagePlayerCraftEvent;
import net.countercraft.movecraft.combat.features.directors.AADirectors;
import net.countercraft.movecraft.combat.features.tracking.events.CraftDamagedByEvent;
import net.countercraft.movecraft.combat.features.tracking.events.CraftFireWeaponEvent;
import net.countercraft.movecraft.combat.features.tracking.types.Fireball;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class FireballTracking implements Listener {
    @NotNull
    private final DamageTracking manager;
    @NotNull
    private final AADirectors directors;


    public FireballTracking(@NotNull DamageTracking manager, @NotNull AADirectors directors) {
        this.manager = manager;
        this.directors = directors;
    }

    public void damagedCraft(@NotNull PlayerCraft craft, @NotNull org.bukkit.entity.Fireball fireball) {
        List<MetadataValue> meta = fireball.getMetadata("MCC-Sender");
        if (meta.isEmpty())
            return;

        UUID sender = UUID.fromString(meta.get(0).asString());
        Player cause = MovecraftCombat.getInstance().getServer().getPlayer(sender);
        if (cause == null || !cause.isOnline())
            return;

        DamageRecord damageRecord = new DamageRecord(cause, craft.getPilot(), new Fireball());
        Bukkit.getPluginManager().callEvent(new CraftDamagedByEvent(craft, damageRecord));
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(@NotNull ProjectileLaunchEvent e) {
        if (!DamageTracking.EnableFireballTracking)
            return;
        if (!(e.getEntity() instanceof SmallFireball))
            return;

        SmallFireball fireball = (SmallFireball) e.getEntity();
        Craft craft = MathUtils.fastNearestCraftToLoc(CraftManager.getInstance().getCrafts(), fireball.getLocation());
        if (!(craft instanceof PlayerCraft))
            return;
        if (!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;

        PlayerCraft playerCraft = (PlayerCraft) craft;

        Player sender = playerCraft.getPilot();
        if (sender == null)
            return;

        fireball.setMetadata("MCC-Sender", new FixedMetadataValue(MovecraftCombat.getInstance(), sender.getUniqueId().toString()));

        CraftFireWeaponEvent event = new CraftFireWeaponEvent(playerCraft, new Fireball());
        Bukkit.getPluginManager().callEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(@NotNull ProjectileHitEvent e) {
        if (!DamageTracking.EnableFireballTracking)
            return;
        if (!(e.getEntity() instanceof org.bukkit.entity.Fireball))
            return;

        org.bukkit.entity.Fireball fireball = (org.bukkit.entity.Fireball) e.getEntity();
        Craft craft = MathUtils.fastNearestCraftToLoc(CraftManager.getInstance().getCrafts(), fireball.getLocation());
        if (!(craft instanceof PlayerCraft))
            return;
        if (!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;

        damagedCraft((PlayerCraft) craft, fireball);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosionDamagePlayerCraft(@NotNull ExplosionDamagePlayerCraftEvent e) {
        if (!DamageTracking.EnableFireballTracking)
            return;
        if (!(e.getDamaging() instanceof org.bukkit.entity.Fireball))
            return;

        damagedCraft((PlayerCraft) e.getCraft(), (org.bukkit.entity.Fireball) e.getDamaging());
    }
}
