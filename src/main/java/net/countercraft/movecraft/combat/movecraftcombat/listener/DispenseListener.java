package net.countercraft.movecraft.combat.movecraftcombat.listener;

import org.jetbrains.annotations.NotNull;
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
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.TNTTracking;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public class DispenseListener implements Listener {
    @EventHandler
    public void dispenseEvent(BlockDispenseEvent e) {
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
        if(!subtract(d, e.getItem()))
            return;

        // Spawn TNT
        Location l = e.getVelocity().toLocation(d.getLocation().getWorld());
        TNTPrimed tnt = (TNTPrimed) l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
        tnt.setVelocity(getTNTVector());

        // Find nearest craft
        Craft craft = MovecraftCombat.fastNearestCraftToLoc(e.getBlock().getLocation());
        if(craft == null)
            return;
        if(!craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation())))
            return;

        // Report to tracking
        TNTTracking.getInstance().dispensedTNT(craft.getNotificationPlayer(), tnt);
    }

    @NotNull
    private Vector getTNTVector() {
        Vector v = new Vector(0, 0.2, 0);
        double angle = Math.random() * 2 * Math.PI;
        v.setX(0.02 * Math.cos(angle));
        v.setZ(0.02 * Math.sin(angle));
        return v;
    }

    private boolean subtract(@NotNull Dispenser d, @NotNull ItemStack item) {
        for(int i = 0; i < d.getInventory().getSize(); i++) {
            ItemStack temp = d.getInventory().getItem(i);
            if(temp == null || !item.isSimilar(temp))
                continue;
            int count = temp.getAmount();
            if(count < 1)
                continue;;
            temp.setAmount(--count);
            d.getInventory().setItem(i, temp);
            return true;
        }
        return false;
    }
}
