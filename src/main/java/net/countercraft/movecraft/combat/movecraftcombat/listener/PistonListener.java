package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.util.Vector;

public class PistonListener implements Listener
{
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(!Config.ReImplementTranslocation)
            return;

        Location headLocation = e.getBlock().getLocation().add(blockFaceToVector(e.getDirection()));

        for(Entity ent : e.getBlock().getWorld().getNearbyEntities(headLocation, 1D, 1D, 1D)) {
            if(ent.getType() != EntityType.PRIMED_TNT)
                continue;
            if(!ent.getLocation().getBlock().getLocation().equals(headLocation) &&
                    !ent.getLocation().getBlock().getLocation().equals(e.getBlock().getLocation()))
                continue;

            ent.teleport(getMoveLocation(ent.getLocation(), e.getBlock().getLocation(), blockFaceToVector(e.getDirection()).multiply(-1D)));
        }
    }

    //The method BlockFace.getDirection() is only present in 1.13+ therefore a custom method must be used
    private Vector blockFaceToVector(BlockFace face) {
        if(face == BlockFace.NORTH)
            return new Vector(0D, 0D, -1D);
        else if(face == BlockFace.SOUTH)
            return new Vector(0D, 0D, 1D);
        else if(face == BlockFace.EAST)
            return new Vector(1D, 0D, 0D);
        else if(face == BlockFace.WEST)
            return new Vector(-1D, 0D, 0D);
        else if(face == BlockFace.UP)
            return new Vector(0D, 1D, 0D);
        else if(face == BlockFace.DOWN)
            return new Vector(0D, -1D, 0D);

        return new Vector();
    }

    private Location getMoveLocation(Location entityLocation, Location blockLocation, Vector direction)
    {
        if(direction.getX() != 0)
            entityLocation.setX(blockLocation.getX() + direction.getX());
        else if(direction.getY() != 0)
            entityLocation.setY(blockLocation.getY() + direction.getY());
        else if(direction.getZ() != 0)
            entityLocation.setZ(blockLocation.getZ() + direction.getZ());

        return entityLocation;
    }
}
