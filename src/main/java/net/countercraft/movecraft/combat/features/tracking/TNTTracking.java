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
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
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

    private boolean subtractItem(@NotNull Inventory inv, @NotNull ItemStack item) {
        int count = item.getAmount();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack temp = inv.getItem(i);
            if (temp == null || !temp.isSimilar(item))
                continue;

            if (temp.getAmount() <= count) {
                count -= temp.getAmount();
                inv.remove(temp);
            } else {
                temp.setAmount(temp.getAmount() - count);
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockDispense(@NotNull BlockDispenseEvent e) {
        if (!DamageTracking.EnableTNTTracking)
            return;
        if (e.getBlock().getType() != Material.DISPENSER || e.getItem().getType() != Material.TNT)
            return;

        // Cancel dispense event
        e.setCancelled(true);

        // Subtract item yourself
        Dispenser d = (Dispenser) e.getBlock().getState();
        Inventory inv = d.getInventory();
        if (!subtractItem(inv, e.getItem())) {
            Bukkit.getScheduler().runTask(MovecraftCombat.getInstance(), () -> {
                subtractItem(inv, e.getItem());
            });
        }

        // Spawn TNT
        Location l = e.getVelocity().toLocation(e.getBlock().getWorld());
        TNTPrimed tnt = (TNTPrimed) e.getBlock().getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
        Vector velocity = getTNTVector();
        tnt.setVelocity(velocity);
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            p.playSound(l, Sound.ENTITY_TNT_PRIMED, 1.5f, 1.5f);
        }

        // Find nearest craft
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getBlock().getLocation());
        if (!(craft instanceof PlayerCraft))
            return;
        if (!craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation())))
            return;

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
