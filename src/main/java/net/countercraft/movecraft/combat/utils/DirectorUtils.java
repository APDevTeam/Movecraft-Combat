package net.countercraft.movecraft.combat.utils;

import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.features.Directors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class DirectorUtils {
    @Nullable
    public static Block getDirectorBlock(@NotNull Player player) {
        Iterator<Block> itr = new BlockIterator(player, Math.min(Config.CannonDirectorRange, distanceToRender(player.getLocation())));
        while (itr.hasNext()) {
            Block block = itr.next();
            Material material = block.getType();
            if(Directors.Transparent == null) {
                if (!material.equals(Material.AIR))
                    return block;
            }
            else {
                if(!Directors.Transparent.contains(material))
                    return block;
            }
        }
        return null;
    }


    private static int distanceToRender(@NotNull Location location) {
        int chunkDisplacementX,chunkDisplacementZ;
        int renderDistance = Bukkit.getServer().getViewDistance() << 4;
        int chunkX = location.getChunk().getX() << 4; //minimum x and z positions in the chunk
        int chunkZ = location.getChunk().getZ() << 4;
        if (location.getDirection().getX() > 0) //get the number that must be added to the coordinate to move it
            chunkDisplacementX = 15 - location.getBlockX()+chunkX; //to the edge of the chunk
        else
            chunkDisplacementX = chunkX - location.getBlockX();

        if (location.getDirection().getZ() > 0)
            chunkDisplacementZ = 15 - location.getBlockZ()+chunkZ;
        else
            chunkDisplacementZ = chunkZ - location.getBlockZ();

        //add a quarter rotation to make the maths easier
        //now theta = 0 is in the positive X direction
        double theta = Math.toRadians((location.getYaw() + 90F) % 360F);

        //We then take the sine of the angle times the X magnitude or the sine of the angle times the z magnitude,
        //whichever is smaller to establish the distance the ray travels before bumping into either edge of render.
        double horizontalDistance = Math.min(Math.abs((renderDistance + Math.abs(chunkDisplacementX))/Math.cos(theta)),
                Math.abs((renderDistance+Math.abs(chunkDisplacementZ))/Math.sin(theta)));
        double finalDistance = Math.min(Math.abs(horizontalDistance / Math.cos(Math.toRadians(location.getPitch()))),
                Math.abs(location.getBlockY()/Math.sin(Math.toRadians(location.getPitch())))); //and now vertical distance
        return (int) finalDistance; //casting to an int will floor the result, giving us a bit of safety.
    }
}