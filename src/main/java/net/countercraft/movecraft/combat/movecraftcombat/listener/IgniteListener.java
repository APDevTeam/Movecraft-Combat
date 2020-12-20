package net.countercraft.movecraft.combat.movecraftcombat.listener;


import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.combat.movecraftcombat.utils.WorldGuard6Utils;
import net.countercraft.movecraft.combat.movecraftcombat.utils.LegacyUtils;
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
            Block testBlock = event.getBlock().getRelative(-1, 0, 0);
            if (!testBlock.getType().isBurnable())
                testBlock = event.getBlock().getRelative(1, 0, 0);
            if (!testBlock.getType().isBurnable())
                testBlock = event.getBlock().getRelative(0, 0, -1);
            if (!testBlock.getType().isBurnable())
                testBlock = event.getBlock().getRelative(0, 0, 1);

            if (!testBlock.getType().isBurnable()) {
                return;
            }

            // check to see if fire spread is allowed, don't check if worldguard integration is not enabled
            if(!isFireSpreadAllowed(event.getBlock().getLocation())) {
                return;
            }
            testBlock.setType(Material.AIR);
        }
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
        if (craft != null) {
            craft.getHitBox().add(MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation()));
        }
    }

    private boolean isFireSpreadAllowed(Location l) {
        if(Movecraft.getInstance().getWorldGuardPlugin() != null && (Settings.WorldGuardBlockMoveOnBuildPerm ||Settings.WorldGuardBlockSinkOnPVPPerm)) {
            if (LegacyUtils.getInstance().isLegacy()) {
                if (!WorldGuard6Utils.locationAllowsFireSpread(l)) {
                    return false;
                }
            } else {
                RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(l.getWorld()));
                ApplicableRegionSet set = manager.getApplicableRegions(BlockVector3.at(l.getX(), l.getY(), l.getZ()));
                for (ProtectedRegion region : set) {
                    if (region.getFlag(Flags.FIRE_SPREAD) == StateFlag.State.DENY) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}