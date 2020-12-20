package net.countercraft.movecraft.combat.movecraftcombat.utils;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class WorldGuard6Utils {

    private static Method GET_REGION_MANAGER;
    private static Method GET_APPLICABLE_REGIONS;

    static {
        try {
            GET_REGION_MANAGER = WorldGuardPlugin.class.getDeclaredMethod("getRegionManager", World.class);
            GET_APPLICABLE_REGIONS = RegionManager.class.getDeclaredMethod("getApplicableRegions", Location.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            GET_REGION_MANAGER = null;
            GET_APPLICABLE_REGIONS = null;
        }
    }

    private static ArrayList<MovecraftLocation> getHitboxCorners(@NotNull HitBox hitbox) {
        ArrayList<MovecraftLocation> corners = new ArrayList<>();
        corners.add(new MovecraftLocation(hitbox.getMinX(), hitbox.getMinY(), hitbox.getMinZ()));
        corners.add(new MovecraftLocation(hitbox.getMinX(), hitbox.getMinY(), hitbox.getMaxZ()));
        corners.add(new MovecraftLocation(hitbox.getMinX(), hitbox.getMaxY(), hitbox.getMinZ()));
        corners.add(new MovecraftLocation(hitbox.getMinX(), hitbox.getMaxY(), hitbox.getMaxZ()));
        corners.add(new MovecraftLocation(hitbox.getMaxX(), hitbox.getMinY(), hitbox.getMinZ()));
        corners.add(new MovecraftLocation(hitbox.getMaxX(), hitbox.getMinY(), hitbox.getMaxZ()));
        corners.add(new MovecraftLocation(hitbox.getMaxX(), hitbox.getMaxY(), hitbox.getMinZ()));
        corners.add(new MovecraftLocation(hitbox.getMaxX(), hitbox.getMaxY(), hitbox.getMaxZ()));
        return corners;
    }

    public static boolean isInAirspace(Craft craft) {
        try {
            for (MovecraftLocation l : getHitboxCorners(craft.getHitBox())) {
                RegionManager manager = (RegionManager) GET_REGION_MANAGER.invoke(MovecraftCombat.getInstance().getWGPlugin(), craft.getW());
                ApplicableRegionSet regions = (ApplicableRegionSet) GET_APPLICABLE_REGIONS.invoke(manager, l.toBukkit(craft.getW()));
                for (ProtectedRegion r : regions.getRegions()) {
                    if (r.getFlag(DefaultFlag.TNT) == StateFlag.State.DENY || r.getFlag(DefaultFlag.PVP) == StateFlag.State.DENY)
                        return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean locationAllowsFireSpread (Location location) {
        try {
            RegionManager manager = (RegionManager) GET_REGION_MANAGER.invoke(MovecraftCombat.getInstance().getWGPlugin(), location.getWorld());
            ApplicableRegionSet regions = (ApplicableRegionSet) GET_APPLICABLE_REGIONS.invoke(manager, location);
            for (ProtectedRegion r : regions.getRegions()) {
                if (r.getFlag(DefaultFlag.FIRE_SPREAD) == StateFlag.State.DENY)
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
