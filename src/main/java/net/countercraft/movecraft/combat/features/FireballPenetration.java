package net.countercraft.movecraft.combat.features;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.util.Tags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;

public class FireballPenetration implements Listener {
    public static boolean EnableFireballPenetration = true;

    public static void load(@NotNull FileConfiguration config) {
        EnableFireballPenetration = config.getBoolean("EnableFireballPenetration", false);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockIgnite(@NotNull BlockIgniteEvent e) {
        if (!EnableFireballPenetration)
            return;
        if (e.getCause() != BlockIgniteEvent.IgniteCause.FIREBALL)
            return;
        if (e.getIgnitingEntity() == null)
            return;

        Block sourceBlock = e.getBlock();
        Block testBlock = sourceBlock.getRelative(BlockFace.EAST);
        if (!testBlock.getType().isBurnable())
            testBlock = sourceBlock.getRelative(BlockFace.WEST);
        if (!testBlock.getType().isBurnable())
            testBlock = sourceBlock.getRelative(BlockFace.NORTH);
        if (!testBlock.getType().isBurnable())
            testBlock = sourceBlock.getRelative(BlockFace.SOUTH);
        if (!testBlock.getType().isBurnable())
            return;

        // To prevent infinite recursion we call the event with SPREAD as the cause
        BlockIgniteEvent igniteEvent = new BlockIgniteEvent(testBlock, BlockIgniteEvent.IgniteCause.SPREAD, e.getIgnitingEntity());
        Bukkit.getPluginManager().callEvent(igniteEvent);
        if (igniteEvent.isCancelled())
            return;

        testBlock.setType(Material.AIR);
    }

    @EventHandler
    public void onProjectileHit(@NotNull ProjectileHitEvent event) {
        if(!(event.getEntity() instanceof Fireball && EnableFireballPenetration)) {
            return;
        }

        Block sourceBlock = event.getHitBlock();
        if (!sourceBlock.getType().isBurnable())
            return;
        if (!Tags.FLUID.contains(sourceBlock.getRelative(event.getHitBlockFace()).getType()))
            return;

        BlockIgniteEvent igniteEvent = new BlockIgniteEvent(sourceBlock, BlockIgniteEvent.IgniteCause.SPREAD, event.getEntity());
        Bukkit.getPluginManager().callEvent(igniteEvent);
        if (igniteEvent.isCancelled()) {
            return;
        }

        sourceBlock.setType(Material.AIR);
    }
}
