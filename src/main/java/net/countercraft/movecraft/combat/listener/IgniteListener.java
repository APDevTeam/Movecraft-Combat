package net.countercraft.movecraft.combat.listener;


import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.hitboxes.MutableHitBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IgniteListener implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        // add surface fires to a craft's hitbox to prevent obstruction by fire
        if (Config.AddFiresToHitbox)
            doAddFiresToHitbox(event);
    }

    @Nullable
    private Craft adjacentCraft(@NotNull Location location) {
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(location);
        if(craft == null)
            return null; //return null if no craft found

        if(MathUtils.locationInHitBox(craft.getHitBox(), location.add(1,0,0))) {
            return craft;
        }
        if(MathUtils.locationInHitBox(craft.getHitBox(), location.add(-1,0,0))) {
            return craft;
        }
        if(MathUtils.locationInHitBox(craft.getHitBox(), location.add(0,1,0))) {
            return craft;
        }
        if(MathUtils.locationInHitBox(craft.getHitBox(), location.add(0,-1,0))) {
            return craft;
        }
        if(MathUtils.locationInHitBox(craft.getHitBox(), location.add(0,0,1))) {
            return craft;
        }
        if(MathUtils.locationInHitBox(craft.getHitBox(), location.add(0,0,-1))) {
            return craft;
        }
        return null;
    }

    private void doAddFiresToHitbox(@NotNull BlockIgniteEvent e) {
        final Craft craft = adjacentCraft(e.getBlock().getLocation());
        if (craft == null || craft.getHitBox().isEmpty())
            return;

        // TODO: This needs to be possible from within Movecraft's API
        MutableHitBox hitbox = (MutableHitBox) craft.getHitBox();

        hitbox.add(MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation()));
    }
}