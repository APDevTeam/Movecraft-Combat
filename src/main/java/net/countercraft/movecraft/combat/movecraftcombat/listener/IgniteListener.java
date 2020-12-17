package net.countercraft.movecraft.combat.movecraftcombat.listener;


import com.sk89q.worldedit.bukkit.BukkitAdapter;
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
import net.countercraft.movecraft.utils.WorldguardUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
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
        final Craft adjacentCraft = adjacentCraft(event.getBlock().getLocation());

        // cannibalizing the anti-spalling code here to keep fireballs from lighting fires inside of moving crafts
        if (!(event.getBlock().getRelative(BlockFace.DOWN).isEmpty() || event.getBlock().getRelative(BlockFace.UP).isEmpty()
                || event.getBlock().getRelative(BlockFace.EAST).isEmpty() || event.getBlock().getRelative(BlockFace.WEST).isEmpty()
                || event.getBlock().getRelative(BlockFace.NORTH).isEmpty() || event.getBlock().getRelative(BlockFace.SOUTH).isEmpty())
                && event.getCause() == BlockIgniteEvent.IgniteCause.FIREBALL) {
            event.setCancelled(true);
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
            if(Movecraft.getInstance().getWorldGuardPlugin() != null && (Settings.WorldGuardBlockMoveOnBuildPerm ||Settings.WorldGuardBlockSinkOnPVPPerm)) {
                RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(testBlock.getWorld()));
                ApplicableRegionSet set = manager.getApplicableRegions(BlockVector3.at(testBlock.getX(), testBlock.getY(), testBlock.getZ()));
                for (ProtectedRegion region : set) {
                    if (region.getFlag(Flags.FIRE_SPREAD) == StateFlag.State.DENY) {
                        return;
                    }
                }
            }
            testBlock.setType(Material.AIR);
        } else if (adjacentCraft != null && Config.AddFiresToHitbox) {
            adjacentCraft.getHitBox().add(MathUtils.bukkit2MovecraftLoc(event.getBlock().getLocation()));
        }
    }

    @Nullable
    private Craft adjacentCraft(@NotNull Location location) {
        for (Craft craft : CraftManager.getInstance().getCraftsInWorld(location.getWorld())) {
            if (!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(location))) {
                continue;
            }
            return craft;
        }
        return null;
    }
}
