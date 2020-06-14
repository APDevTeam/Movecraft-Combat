package net.countercraft.movecraft.combat.movecraftcombat;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.combat.movecraftcombat.sign.*;
import net.countercraft.movecraft.combat.movecraftcombat.directors.*;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public final class MovecraftCombat extends JavaPlugin {
    private static MovecraftCombat instance;
    private CannonDirectorManager cannonDirectors;
    private AADirectorManager aaDirectors;
    public HashSet<Material> transparent = new HashSet<>();

    public static synchronized MovecraftCombat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        saveDefaultConfig();
        Config.Debug = getConfig().getBoolean("Debug", false);
        Config.AADirectorDistance = getConfig().getInt("AADirectorDistance", 50);
        Config.AADirectorRange = getConfig().getInt("AADirectorRange", 120);
        for(String s : getConfig().getStringList("AADirectorsAllowed")) {
            Config.AADirectorsAllowed.add(CraftManager.getInstance().getCraftTypeFromString(s));
        }
        Config.CannonDirectorDistance = getConfig().getInt("CannonDirectorsDistance", 100);
        Config.CannonDirectorRange = getConfig().getInt("CannonDirectorRange", 120);
        for(String s : getConfig().getStringList("CannonDirectorsAllowed")) {
            Config.CannonDirectorsAllowed.add(CraftManager.getInstance().getCraftTypeFromString(s));
        }
        if(getConfig().contains("DurabilityOverride")) {
            Map<String, Object> temp = getConfig().getConfigurationSection("DurabilityOverride").getValues(false);
            Config.DurabilityOverride = new HashMap<>();
            for (String str : temp.keySet()) {
                Config.DurabilityOverride.put(Integer.parseInt(str), (Integer) temp.get(str));
            }
        }
        Config.FireballLifespan = getConfig().getInt("FireballLifespan", 6);
        Config.TracerRateTicks = getConfig().getDouble("TracerRateTicks", 5.0);
        Config.TracerMinDistanceSqrd = getConfig().getLong("TracerMinDistance", 60);
        Config.TracerMinDistanceSqrd *= Config.TracerMinDistanceSqrd;

        getServer().getPluginManager().registerEvents(new AADirectorSign(), this);
        getServer().getPluginManager().registerEvents(new CannonDirectorSign(), this);

        aaDirectors = new AADirectorManager();
        aaDirectors.runTaskTimer(this, 0, 1);
        cannonDirectors = new CannonDirectorManager();
        cannonDirectors.runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public CannonDirectorManager getCannonDirectors() {
        return cannonDirectors;
    }

    public AADirectorManager getAADirectors() {
        return aaDirectors;
    }

    public static Craft fastNearestCraftToLoc(@NotNull Location loc) {
        //TODO: Replace with Movecraft's version
        Craft ret = null;
        long closestDistSquared = Long.MAX_VALUE;
        Set<Craft> craftsList = CraftManager.getInstance().getCraftsInWorld(loc.getWorld());
        for (Craft i : craftsList) {
            int midX = (i.getHitBox().getMaxX() + i.getHitBox().getMinX()) >> 1;
//				int midY=(i.getMaxY()+i.getMinY())>>1; don't check Y because it is slow
            int midZ = (i.getHitBox().getMaxZ() + i.getHitBox().getMinZ()) >> 1;
            long distSquared = (long) (Math.pow(midX -  loc.getX(), 2) + Math.pow(midZ - (int) loc.getZ(), 2));
            if (distSquared < closestDistSquared) {
                closestDistSquared = distSquared;
                ret = i;
            }
        }
        return ret;
    }
}