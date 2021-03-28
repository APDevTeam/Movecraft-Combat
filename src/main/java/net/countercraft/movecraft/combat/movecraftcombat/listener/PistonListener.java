package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class PistonListener implements Listener {
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(!Config.ReImplementTNTTranslocation)
            return;

        Location headLoc = e.getBlock().getRelative(e.getDirection()).getLocation();

        Block moveBlock = e.getBlock().getRelative(e.getDirection().getOppositeFace()).getLocation().getBlock();

        if(!moveBlock.isEmpty() && !moveBlock.isLiquid() && !moveBlock.getType().name().contains("SIGN"))
            return;

        Location moveLoc = new Location(moveBlock.getWorld(), moveBlock.getLocation().getBlockX(), moveBlock.getLocation().getBlockY(), moveBlock.getLocation().getBlockZ());
        moveLoc.add(0.5D, 0.5D, 0.5D);

        for(Entity entity : e.getBlock().getWorld().getNearbyEntities(headLoc, 2D, 2D, 2D)) {
            if(entity.getType() != EntityType.PRIMED_TNT)
                continue;

            if(!entity.getLocation().getBlock().getLocation().equals(headLoc))
                continue;

            entity.teleport(moveLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            entity.setVelocity(new Vector());
        }
    }
}
