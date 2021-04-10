package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.utils.Pair;
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

            Location loc = tnt.getLocation();
            if(isInBlock(loc, piston)) {
                Pair<Double, Double> offsets = getOffset(loc, getCenterLocation(piston));
                if(isInBlock(offsets))
                    searchResults.add(new SearchEntry(tnt, offsets.getLeft(), offsets.getRight()));
            }
            else if(pistonHead != null && isInBlock(loc, pistonHead)) {
                Pair<Double, Double> offsets = getOffset(loc, getCenterLocation(pistonHead));
                if(isInBlock(offsets))
                    searchResults.add(new SearchEntry(tnt, offsets.getLeft(), offsets.getRight()));
            }
        }
        return searchResults;
    }

    private boolean isInBlock(@NotNull Location loc, @Nullable Block b) {
        return loc.getBlock() != b;
    }

    @NotNull
    private Pair<Double, Double> getOffset(@NotNull Location one, @NotNull Location two) {
        double xOffset = one.getX() - two.getX();
        double zOffset = one.getZ() - two.getZ();
        return new Pair<>(xOffset, zOffset);
    }

    private boolean isInBlock(@NotNull Pair<Double, Double> offsets) {
        return Math.abs(offsets.getLeft()) < 0.021 && Math.abs(offsets.getRight()) < 0.021;
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
