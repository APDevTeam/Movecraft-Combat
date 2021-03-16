package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;

public class PistonListener implements Listener
{
    //Block.isPassable() would be perfect for this but only exists in 1.13+
    private final HashSet<Material> passable = new HashSet<>(Arrays.asList(
            Material.AIR,
            Material.WATER,
            Material.SIGN,
            Material.WALL_SIGN
    ));

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(!Config.ReImplementTranslocation)
            return;

        Location headLocation = e.getBlock().getLocation().add(blockFaceToVector(e.getDirection()));
        Vector direction = blockFaceToVector(e.getDirection()).multiply(-1D);
        Location moveLocation = e.getBlock().getLocation().add(direction);

        //This is disgusting but LEGACY_WATER does not exist below 1.13
        if(!passable.contains(moveLocation.getBlock().getType()) && !moveLocation.getBlock().getType().name().contains("WATER"))
            return;

        for(Entity ent : e.getBlock().getWorld().getNearbyEntities(headLocation, 2D, 2D, 2D)) {
            if(ent.getType() != EntityType.PRIMED_TNT)
                continue;
            if(!ent.getLocation().getBlock().getLocation().equals(headLocation) &&
                    !ent.getLocation().getBlock().getLocation().equals(e.getBlock().getLocation()))
                continue;

            ent.teleport(getMoveLocation(ent.getLocation(), moveLocation, direction));
            ent.setVelocity(new Vector());
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

    private Location getMoveLocation(Location entityLocation, Location moveLocation, Vector direction)
    {
        if(direction.getX() != 0)
            entityLocation.setX(moveLocation.getX());
        else if(direction.getY() != 0)
            entityLocation.setY(moveLocation.getY());
        else if(direction.getZ() != 0)
            entityLocation.setZ(moveLocation.getZ());

        return entityLocation;
    }
}
