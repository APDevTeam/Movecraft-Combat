package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class PistonListener implements Listener {
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(!Config.ReImplementTNTTranslocation)
            return;

        BlockFace dir = e.getDirection();
        if(e.isSticky())
            dir = dir.getOppositeFace();

        doTranslocation(e.getBlock(), dir, true);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if(!Config.ReImplementTNTTranslocation)
            return;

        doTranslocation(e.getBlock(), e.getDirection().getOppositeFace(), false);
    }

    private void doTranslocation(@NotNull Block piston, @NotNull BlockFace direction, boolean pistonHead) {
        Block moveBlock = piston.getRelative(direction.getOppositeFace());
        if(!isValidMoveBlock(moveBlock))
            return;

        HashSet<SearchEntry> searchResults = getTNT(piston, pistonHead ? piston.getRelative(direction) : null);

        Location moveLoc = getCenterLocation(moveBlock);
        for(SearchEntry se : searchResults) {
            se.translocateTo(moveLoc);
        }
    }

    private boolean isValidMoveBlock(@NotNull Block moveBlock) {
        if(moveBlock.isEmpty() || moveBlock.isLiquid())
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
    private HashSet<SearchEntry> getTNT(@NotNull Block piston, @Nullable Block pistonHead) {
        HashSet<SearchEntry> searchResults = new HashSet<>();
        for(Entity e : piston.getWorld().getEntities()) {
            if(!e.isValid() || e.getType() != EntityType.PRIMED_TNT)
                continue;

            TNTPrimed tnt = (TNTPrimed) e;
            if(tnt.getFuseTicks() <= 0)
                continue;

            SearchEntry pistonEntry = getEntry(tnt, piston);
            if(pistonEntry != null)
                searchResults.add(pistonEntry);
            else if(pistonHead != null) {
                SearchEntry headEntry = getEntry(tnt, pistonHead);
                if(headEntry != null)
                    searchResults.add(headEntry);
            }
        }
        return searchResults;
    }

    @Nullable
    private SearchEntry getEntry(@NotNull TNTPrimed tnt, @NotNull Block block) {
        Location tntLoc = tnt.getLocation();
        Location blockLoc = getCenterLocation(block);
        if(tntLoc.getBlockX() != blockLoc.getBlockX()
                || tntLoc.getBlockY() != blockLoc.getBlockY()
                || tntLoc.getBlockZ() != tntLoc.getBlockZ())
            return null;

        double xOffset = tntLoc.getX() - blockLoc.getX();
        if(Math.abs(xOffset) > 0.021)
            return null;

        double yOffset = tntLoc.getY() - blockLoc.getY();
        if(Math.abs(yOffset) > 0.021)
            return null;

        double zOffset = tntLoc.getZ() - blockLoc.getZ();
        if(Math.abs(zOffset) > 0.021)
            return null;

        return new SearchEntry(tnt, xOffset, zOffset);
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
