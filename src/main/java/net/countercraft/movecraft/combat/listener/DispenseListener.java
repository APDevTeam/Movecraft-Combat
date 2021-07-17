package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.tracking.TNTTracking;


public class DispenseListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispenseEvent(BlockDispenseEvent e) {
        if(e.isCancelled())
            return;
        if(!Config.EnableTNTTracking)
            return;
        if(e.getBlock().getType() != Material.DISPENSER)
            return;
        if(e.getItem().getType() != Material.TNT)
            return;

        // Cancel dispense event
        e.setCancelled(true);

        // Subtract item yourself
        Dispenser d = (Dispenser) e.getBlock().getState();
        Inventory inv = d.getInventory();
        if(!subtractItem(inv, e.getItem())) {
            Bukkit.getScheduler().runTask(MovecraftCombat.getInstance(), () -> {
                subtractItem(inv, e.getItem());
            });
        }

        // Spawn TNT
        Location l = e.getVelocity().toLocation(e.getBlock().getWorld());
        TNTPrimed tnt = (TNTPrimed) e.getBlock().getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
        Vector velocity = getTNTVector();
        tnt.setVelocity(velocity);

        if(Config.Debug)
            MovecraftCombat.getInstance().getLogger().info("Spawned custom TNT!: " + l + ", " + velocity);

        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            p.playSound(l, Sound.ENTITY_TNT_PRIMED, 1.5f, 1.5f);
        }

        // Find nearest craft
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getBlock().getLocation());
        if(craft == null || !(craft instanceof PlayerCraft))
            return;
        if(!craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation())))
            return;

        // Report to tracking
        PlayerCraft playerCraft = (PlayerCraft) craft;
        TNTTracking.getInstance().dispensedTNT(playerCraft, tnt);
        StatusManager.getInstance().registerEvent(playerCraft.getPlayer());
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
        for(int i = 0; i < inv.getSize(); i++) {
            ItemStack temp = inv.getItem(i);
            if(temp == null || !temp.isSimilar(item))
                continue;

            if(temp.getAmount() <= count) {
                count -= temp.getAmount();
                inv.remove(temp);
            }
            else {
                temp.setAmount(temp.getAmount() - count);
                return true;
            }
        }
        return false;
    }
}