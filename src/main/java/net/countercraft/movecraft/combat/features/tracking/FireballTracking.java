package net.countercraft.movecraft.combat.features.tracking;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.event.ExplosionDamagePlayerCraftEvent;
import net.countercraft.movecraft.combat.features.combat.CombatRelease;
import net.countercraft.movecraft.combat.features.directors.AADirectors;
import net.countercraft.movecraft.combat.features.tracking.types.FireballDamage;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.entity.Fireball;
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

    public FireballTracking(@NotNull DamageTracking manager) {
        this.manager = manager;
    }

    public void damagedCraft(@NotNull PlayerCraft craft, @NotNull Fireball fireball) {
        List<MetadataValue> meta = fireball.getMetadata("MCC-Sender");
        if(meta.isEmpty())
            return;

        UUID sender = UUID.fromString(meta.get(0).asString());
        Player cause = MovecraftCombat.getInstance().getServer().getPlayer(sender);
        if(cause == null || !cause.isOnline())
            return;

        manager.addDamageRecord(craft, cause, new FireballDamage());
        CombatRelease.getInstance().registerEvent(craft.getPilot());
    }



    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(@NotNull ProjectileLaunchEvent e) {
        if(!DamageTracking.EnableFireballTracking)
            return;
        if(!(e.getEntity() instanceof SmallFireball))
            return;

        SmallFireball fireball = (SmallFireball) e.getEntity();
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(fireball.getLocation());
        if(!(craft instanceof PlayerCraft))
            return;
        if(!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;

        PlayerCraft playerCraft = (PlayerCraft) craft;

        Player sender;
        if(AADirectors.getInstance() != null && AADirectors.getInstance().hasDirector(playerCraft))
            sender = AADirectors.getInstance().getDirector(playerCraft);
        else
            sender = playerCraft.getPilot();

        if(sender == null)
            return;

        fireball.setMetadata("MCC-Sender", new FixedMetadataValue(MovecraftCombat.getInstance(), sender.getUniqueId().toString()));


        CombatRelease.getInstance().registerEvent(playerCraft.getPilot());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(@NotNull ProjectileHitEvent e) {
        if(!DamageTracking.EnableFireballTracking)
            return;
        if(!(e.getEntity() instanceof Fireball))
            return;

        Fireball fireball = (Fireball) e.getEntity();
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(fireball.getLocation());
        if(!(craft instanceof PlayerCraft))
            return;
        if(!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;

        damagedCraft((PlayerCraft) craft, fireball);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosionDamagePlayerCraft(@NotNull ExplosionDamagePlayerCraftEvent e) {
        if(!DamageTracking.EnableFireballTracking)
            return;
        if(!(e.getDamaging() instanceof Fireball))
            return;

        damagedCraft((PlayerCraft) e.getCraft(), (Fireball) e.getDamaging());
    }
}
