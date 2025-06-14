package net.countercraft.movecraft.combat.features.tracking;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.event.ExplosionDamagePlayerCraftEvent;
import net.countercraft.movecraft.combat.features.directors.CannonDirectors;
import net.countercraft.movecraft.combat.features.tracking.events.CraftDamagedByEvent;
import net.countercraft.movecraft.combat.features.tracking.events.CraftFireWeaponEvent;
import net.countercraft.movecraft.combat.features.tracking.types.TNTCannon;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class TNTTracking implements Listener {
    @NotNull
    private final DamageTracking manager;
    @NotNull
    private final CannonDirectors directors;


    public TNTTracking(@NotNull DamageTracking manager, @NotNull CannonDirectors directors) {
        this.manager = manager;
        this.directors = directors;
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosionDamagePlayerCraft(@NotNull ExplosionDamagePlayerCraftEvent e) {
        if (!DamageTracking.EnableTNTTracking)
            return;
        if (!(e.getDamaging() instanceof TNTPrimed))
            return;

        TNTPrimed tnt = (TNTPrimed) e.getDamaging();
        List<MetadataValue> meta = tnt.getMetadata("MCC-Sender");
        if (meta.isEmpty())
            return;

        UUID sender = UUID.fromString(meta.get(0).asString());
        Player cause = Bukkit.getServer().getPlayer(sender);
        if (cause == null || !cause.isOnline())
            return;

        var craft = e.getDamaged();
        DamageRecord damageRecord = new DamageRecord(cause, craft.getPilot(), new TNTCannon());
        Bukkit.getPluginManager().callEvent(new CraftDamagedByEvent(craft, damageRecord));
    }


    @NotNull
    private Vector getTNTVector() {
        Vector v = new Vector(0, 0.2, 0);
        double angle = Math.random() * 2 * Math.PI;
        v.setX(0.02 * Math.cos(angle));
        v.setZ(0.02 * Math.sin(angle));
        return v;
    }

    @EventHandler
    public void onEntitySpawn(@NotNull EntitySpawnEvent e) {
        if (!DamageTracking.EnableTNTTracking)
            return;
        if (!e.getEntityType().equals(EntityType.TNT))
            return;
        TNTPrimed tnt = (TNTPrimed) e.getEntity();

        // Find nearest craft
        Craft craft = MathUtils.fastNearestCraftToLoc(CraftManager.getInstance().getCrafts(),
                tnt.getLocation());
        if (craft instanceof SinkingCraft && DamageTracking.DisableSinkingCraftTNT) {
            e.setCancelled(true);
            return;
        }
        else if (craft != null && craft.getDisabled() && DamageTracking.DisableDisabledCraftTNT) {
            e.setCancelled(true);
            return;
        }

        if (!(craft instanceof PlayerCraft))
            return;
        if (!craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(tnt.getLocation()))) {
            //check adjacent blocks
            boolean found = false;
            Block center = tnt.getLocation().getBlock();
            for (BlockFace face : BlockFace.values()) {
                if (craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(center.getRelative(face).getLocation()))) {
                    found = true;
                    break;
                }
            }
            if (!found) return;
        }

        // Report to tracking
        PlayerCraft playerCraft = (PlayerCraft) craft;
        Player sender;
        if (directors.hasDirector(playerCraft))
            sender = directors.getDirector(playerCraft);
        else
            sender = playerCraft.getPilot();
        if (sender == null)
            return;

        tnt.setMetadata("MCC-Sender", new FixedMetadataValue(MovecraftCombat.getInstance(), sender.getUniqueId().toString()));

        CraftFireWeaponEvent event = new CraftFireWeaponEvent(playerCraft, new TNTCannon());
        Bukkit.getPluginManager().callEvent(event);
    }
}
