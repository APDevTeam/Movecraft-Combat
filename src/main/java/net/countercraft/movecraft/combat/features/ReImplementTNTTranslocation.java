package net.countercraft.movecraft.combat.features;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ReImplementTNTTranslocation implements Listener {
    public static boolean ReImplementTNTTranslocation = false;

    public static void load(@NotNull FileConfiguration config) {
        ReImplementTNTTranslocation = config.getBoolean("ReImplementTNTTranslocation", false);
    }


    private void doTranslocation(@NotNull Block piston, @NotNull BlockFace direction, @Nullable Block pistonHead) {
        Block moveBlock = piston.getRelative(direction.getOppositeFace());
        if (!isValidMoveBlock(moveBlock))
            return;

        Set<SearchEntry> searchResults = getTNT(piston, pistonHead, direction);

        Location moveLoc = getCenterLocation(moveBlock);
        for (SearchEntry se : searchResults)
            se.translocateTo(moveLoc);
    }

    private boolean isValidMoveBlock(@NotNull Block moveBlock) {
        if (moveBlock.isEmpty() || moveBlock.isLiquid())
            return true;

        String typeName = moveBlock.getType().name();
        return typeName.contains("SIGN") || typeName.contains("BUTTON")
                || typeName.contains("LEVER") || typeName.contains("TORCH");
    }

    @NotNull
    private Location getCenterLocation(@NotNull Block block) {
        Location loc = new Location(block.getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
        loc.add(0.5D, 0.5D, 0.5D);
        return loc;
    }

    @NotNull
    private Set<SearchEntry> getTNT(@NotNull Block piston, @Nullable Block pistonHead, @NotNull BlockFace direction) {
        Set<SearchEntry> searchResults = new HashSet<>();
        for (Entity e : piston.getWorld().getEntities()) {
            if (!e.isValid() || e.getType() != EntityType.TNT)
                continue;

            TNTPrimed tnt = (TNTPrimed) e;
            if (tnt.getFuseTicks() <= 0)
                continue;

            SearchEntry pistonEntry = getEntry(tnt, piston, null);
            if (pistonEntry != null) {
                searchResults.add(pistonEntry);
                continue;
            }

            if (pistonHead != null) {
                SearchEntry headEntry = getEntry(tnt, pistonHead, direction);
                if (headEntry == null)
                    continue;

                searchResults.add(headEntry);
            }
        }
        return searchResults;
    }

    @Nullable
    private SearchEntry getEntry(@NotNull TNTPrimed tnt, @NotNull Block block, @Nullable BlockFace direction) {
        // Null direction means this is a loose check, only for center within block.
        // A non-null direction means this is a strict check, which means it must be within 0.021 offset in everything but the opposite of the direction given

        Location tntLoc = tnt.getLocation();
        Location blockLoc = getCenterLocation(block);

        if (tntLoc.getBlockX() != blockLoc.getBlockX()
                || tntLoc.getBlockY() != blockLoc.getBlockY()
                || tntLoc.getBlockZ() != blockLoc.getBlockZ())
            return null;


        double xOffset = tntLoc.getX() - blockLoc.getX();
        if (direction != null && Math.abs(xOffset) > 0.021) {
            if (!(xOffset < 0.0 && direction == BlockFace.EAST)
                    && !(xOffset > 0.0 && direction == BlockFace.WEST))
                return null;
        }


        double zOffset = tntLoc.getZ() - blockLoc.getZ();
        if (direction != null && Math.abs(zOffset) > 0.021) {
            if (!(zOffset > 0.0 && direction == BlockFace.NORTH)
                    && !(zOffset < 0.0 && direction == BlockFace.SOUTH))
                return null;
        }

        return new SearchEntry(tnt, xOffset, zOffset);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (!ReImplementTNTTranslocation)
            return;

        doTranslocation(e.getBlock(), e.getDirection().getOppositeFace(), null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(@NotNull BlockPistonRetractEvent e) {
        if (!ReImplementTNTTranslocation)
            return;

        BlockFace dir = e.getDirection();
        if (e.isSticky())
            dir = dir.getOppositeFace();

        doTranslocation(e.getBlock(), dir, e.getBlock().getRelative(dir));
    }

    private static class SearchEntry {
        private final TNTPrimed tnt;
        private final double xOffset;
        private final double zOffset;

        public SearchEntry(TNTPrimed tnt, double xOffset, double zOffset) {
            this.tnt = tnt;
            this.xOffset = xOffset;
            this.zOffset = zOffset;
        }

        public void translocateTo(@NotNull Location loc) {
            Location moveLoc = new Location(loc.getWorld(), loc.getX() + xOffset, loc.getY(), loc.getZ() + zOffset);
            tnt.teleport(moveLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }
}
