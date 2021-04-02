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
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class PistonListener implements Listener {
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(!Config.ReImplementTNTTranslocation)
            return;

        doPistonTranslocation(e.getBlock(), e.getDirection());
    }

    private void doPistonTranslocation(Block piston, BlockFace direction) {
        Block moveBlock = piston.getRelative(direction.getOppositeFace()).getLocation().getBlock();
        if(!moveBlock.isEmpty() && !moveBlock.isLiquid() && !moveBlock.getType().name().contains("SIGN"))
            return;

        Location moveLoc = getMoveLocation(moveBlock);

        for(TNTPrimed tnt : getTNTInPiston(piston, direction)) {
            tnt.teleport(moveLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            tnt.setVelocity(new Vector());
        }
    }

    @NotNull
    private Location getMoveLocation(@NotNull Block moveBlock) {
        Location moveLoc = new Location(moveBlock.getWorld(), moveBlock.getLocation().getBlockX(), moveBlock.getLocation().getBlockY(), moveBlock.getLocation().getBlockZ());
        moveLoc.add(0.5D, 0.5D, 0.5D);
        return moveLoc;
    }

    @NotNull
    private HashSet<TNTPrimed> getTNTInPiston(@NotNull Block piston, @NotNull BlockFace direction) {
        Block head = piston.getRelative(direction);
        HashSet<TNTPrimed> tntSet = new HashSet<>();
        for(Entity e : piston.getWorld().getEntities()) {
            if(!e.isValid() || e.getType() != EntityType.PRIMED_TNT)
                continue;

            TNTPrimed tnt = (TNTPrimed) e;
            if(tnt.getFuseTicks() <= 0)
                continue;

            Location loc = tnt.getLocation();
            if(!isInBlock(loc, piston) && !isInBlock(loc, head))
                continue;

            tntSet.add(tnt);
        }
        return tntSet;
    }

    private boolean isInBlock(@NotNull Location l, @NotNull Block b) {
        return l.getBlockX() == b.getLocation().getBlockX()
                && l.getBlockY() == b.getLocation().getBlockY()
                && l.getBlockZ() == b.getLocation().getBlockZ();
    }
}
