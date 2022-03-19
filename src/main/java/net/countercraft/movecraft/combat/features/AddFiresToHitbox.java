package net.countercraft.movecraft.combat.features;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.hitboxes.MutableHitBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddFiresToHitbox implements Listener {
    public static boolean AddFiresToHitbox = true;

    public static void load(@NotNull FileConfiguration config) {
        AddFiresToHitbox = config.getBoolean("AddFiresToHitbox", true);
    }


    @Nullable
    private Craft adjacentCraft(@NotNull Location location) {
        Craft craft = MathUtils.fastNearestCraftToLoc(CraftManager.getInstance().getCrafts(), location);
        if (craft == null)
            return null; //return null if no craft found

        if (MathUtils.locationInHitBox(craft.getHitBox(), location.add(1, 0, 0))
                || MathUtils.locationInHitBox(craft.getHitBox(), location.add(-1, 0, 0))
                || MathUtils.locationInHitBox(craft.getHitBox(), location.add(0, 1, 0))
                || MathUtils.locationInHitBox(craft.getHitBox(), location.add(0, -1, 0))
                || MathUtils.locationInHitBox(craft.getHitBox(), location.add(0, 0, 1))
                || MathUtils.locationInHitBox(craft.getHitBox(), location.add(0, 0, -1)))
            return craft;

        return null;
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockIgnite(@NotNull BlockIgniteEvent e) {
        Craft craft = adjacentCraft(e.getBlock().getLocation());
        if (craft == null || craft.getHitBox().isEmpty())
            return;
        if (!(craft.getHitBox() instanceof MutableHitBox))
            return;

        MutableHitBox hitbox = (MutableHitBox) craft.getHitBox();
        hitbox.add(MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent e) {
        // Allow punching fire out on crafts
        if (e.getBlock().getType() != Material.FIRE)
            return;
        Craft craft = adjacentCraft(e.getBlock().getLocation());
        if (craft == null || craft.getHitBox().isEmpty())
            return;
        if (!(craft.getHitBox() instanceof MutableHitBox))
            return;

        MutableHitBox hitbox = (MutableHitBox) craft.getHitBox();
        hitbox.remove(MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation()));
    }
}
