package net.countercraft.movecraft.combat.movecraftcombat.listener;


import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.combat.movecraftcombat.utils.WorldGuard6Utils;
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
    @EventHandler(priority = EventPriority.LOW)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // replace blocks with fire occasionally, to prevent fast craft from simply ignoring fire
        if (Config.EnableFireballPenetration && event.getCause() == BlockIgniteEvent.IgniteCause.FIREBALL) {
            doFireballPenetration(event);
        }

        // add surface fires to a craft's hitbox to prevent obstruction by fire
        if (Config.AddFiresToHitbox) {
            doAddFiresToHitbox(event);
        }
    }

    @Nullable
    private Craft adjacentCraft(@NotNull Location location) {
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(location);
        if(craft == null) {
            return null; //return null if no craft found
        }

        //TODO move this to a locIsAdjacentToCraft method
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

    private void doAddFiresToHitbox(BlockIgniteEvent e) {
        final Craft craft = adjacentCraft(e.getBlock().getLocation());
        if (craft == null || craft.getHitBox().isEmpty())
            return;

        craft.getHitBox().add(MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation()));
    }

    private void doFireballPenetration(BlockIgniteEvent e) {
        Block testBlock = e.getBlock().getRelative(-1, 0, 0);
        if (!testBlock.getType().isBurnable())
            testBlock = e.getBlock().getRelative(1, 0, 0);
        if (!testBlock.getType().isBurnable())
            testBlock = e.getBlock().getRelative(0, 0, -1);
        if (!testBlock.getType().isBurnable())
            testBlock = e.getBlock().getRelative(0, 0, 1);

        if (!testBlock.getType().isBurnable()) {
            return;
        }

        // check to see if fire spread is allowed, don't check if worldguard integration is not enabled
        if(!isFireSpreadAllowed(e.getBlock().getLocation())) {
            return;
        }
        testBlock.setType(Material.AIR);
    }

    private boolean isFireSpreadAllowed(Location l) {
        if(MovecraftCombat.getInstance().getWGPlugin() == null) {
            return true;
        }
        return WorldGuard6Utils.isFireSpreadAllowed(l);
    }
}