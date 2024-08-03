package net.countercraft.movecraft.combat.features;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class Bleedfix implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent e) {

        if (e.getEntityType() != EntityType.PRIMED_TNT)
            return;

        Set<Block> removeList = new HashSet<>();
        for (Block b : e.blockList()) {
            // remove the block if no adjacent blocks are air (IE: the explosion skipped a block)
            if (!nextToAir(b)) {
                removeList.add(b);
            }
        }

        e.blockList().removeAll(removeList);
    }

    private boolean nextToAir(@NotNull Block b) {
        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.SELF) continue;

            if (b.getRelative(face).isEmpty())
                return true;
        }

        return false;
    }
}
