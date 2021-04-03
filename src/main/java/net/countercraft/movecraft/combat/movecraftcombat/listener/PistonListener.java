package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

public class PistonListener implements Listener {
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(!Config.ReImplementTNTTranslocation)
            return;

        doTranslocation(e.getBlock(), e.getDirection(), true);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if(!Config.ReImplementTNTTranslocation)
            return;

        doTranslocation(e.getBlock(), e.getDirection(), false);
    }

    private void doTranslocation(@NotNull Block piston, @NotNull BlockFace direction, boolean pistonHead) {
        Block moveBlock = piston.getRelative(direction.getOppositeFace());
        if(!isValidMoveBlock(moveBlock))
            return;

        Location moveLoc = getCenterLocation(moveBlock);

        ArrayList<Block> blocks = new ArrayList<>();
        blocks.add(piston);
        if(pistonHead)
            blocks.add(piston.getRelative(direction));

        for(TNTPrimed tnt : getTNTInBlocks(blocks, piston.getWorld())) {
            tnt.teleport(moveLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            tnt.setVelocity(new Vector());
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
    private Location getCenterLocation(@NotNull Block moveBlock) {
        Location moveLoc = new Location(moveBlock.getWorld(), moveBlock.getLocation().getBlockX(), moveBlock.getLocation().getBlockY(), moveBlock.getLocation().getBlockZ());
        moveLoc.add(0.5D, 0.5D, 0.5D);
        return moveLoc;
    }

    @NotNull
    private HashSet<TNTPrimed> getTNTInBlocks(@NotNull ArrayList<Block> blocks, @NotNull World w) {
        HashSet<TNTPrimed> tntSet = new HashSet<>();
        for(Entity e : w.getEntities()) {
            if(!e.isValid() || e.getType() != EntityType.PRIMED_TNT)
                continue;

            TNTPrimed tnt = (TNTPrimed) e;
            if(tnt.getFuseTicks() <= 0)
                continue;

            Location loc = tnt.getLocation();
            for(Block b : blocks) {
                if(isInBlock(loc, b)) {
                    tntSet.add(tnt);
                    break;
                }
            }
        }

        return tntSet;
    }

    private boolean isInBlock(@NotNull Location l, @NotNull Block b) {
        return l.getBlockX() == b.getLocation().getBlockX()
                && l.getBlockY() == b.getLocation().getBlockY()
                && l.getBlockZ() == b.getLocation().getBlockZ();
    }
}
