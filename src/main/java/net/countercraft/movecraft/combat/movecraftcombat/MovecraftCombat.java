package net.countercraft.movecraft.combat.movecraftcombat;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.combat.movecraftcombat.sign.*;
import net.countercraft.movecraft.combat.movecraftcombat.listener.*;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageManager;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;
import net.countercraft.movecraft.combat.movecraftcombat.directors.*;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public final class MovecraftCombat extends JavaPlugin {
    private static MovecraftCombat instance;
    private AADirectorManager aaDirectors;
    private CannonDirectorManager cannonDirectors;
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
        Config.EnableContactExplosives = getConfig().getBoolean("EnableContactExplosives", true);
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
        Config.EnableFireballTracking = getConfig().getBoolean("EnableFireballTracking", false);
        Config.EnableTNTTracking = getConfig().getBoolean("EnableTNTTracking", true);
        Config.EnableTorpedoTracking = getConfig().getBoolean("EnableTorpedoTracking", false);
        Config.DamageTimeout = getConfig().getInt("DamageTimeout", 300);
        Config.EnableCombatReleaseTracking = getConfig().getBoolean("EnableCombatReleaseTracking", false);
        Config.EnableCombatReleaseKick = getConfig().getBoolean("EnableCombatReleaseKick", true);
        Config.CombatReleaseBanLength = getConfig().getLong("CombatReleaseBanLength", 60);
        Config.CombatReleaseScuttle = getConfig().getBoolean("CombatReleaseScuttle", true);


        getServer().getPluginManager().registerEvents(new CraftCollisionExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new CraftReleaseListener(), this);
        getServer().getPluginManager().registerEvents(new CraftScuttleListener(), this);
        getServer().getPluginManager().registerEvents(new CraftSinkListener(), this);
        getServer().getPluginManager().registerEvents(new DispenseListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(), this);
        getServer().getPluginManager().registerEvents(new AADirectorSign(), this);
        getServer().getPluginManager().registerEvents(new CannonDirectorSign(), this);


        aaDirectors = new AADirectorManager();
        aaDirectors.runTaskTimer(this, 0, 1);
        cannonDirectors = new CannonDirectorManager();
        cannonDirectors.runTaskTimer(this, 0, 1);
        DamageManager damageTracking = new DamageManager();
        damageTracking.runTaskTimer(this, 0, 12000);    // Every 10 minutes
        StatusManager statusTracking = new StatusManager();
        statusTracking.runTaskTimer(this, 0, 200);      // Every 10 seconds

        transparent.add(Material.AIR);
        transparent.add(Material.GLASS);
        transparent.add(Material.THIN_GLASS);
        transparent.add(Material.STAINED_GLASS);
        transparent.add(Material.STAINED_GLASS_PANE);
        transparent.add(Material.IRON_FENCE);
        transparent.add(Material.REDSTONE_WIRE);
        transparent.add(Material.IRON_TRAPDOOR);
        transparent.add(Material.TRAP_DOOR);
        transparent.add(Material.NETHER_BRICK_STAIRS);
        transparent.add(Material.LEVER);
        transparent.add(Material.STONE_BUTTON);
        transparent.add(Material.WOOD_BUTTON);
        transparent.add(Material.STEP);
        transparent.add(Material.SMOOTH_STAIRS);
        transparent.add(Material.SIGN);
        transparent.add(Material.SIGN_POST);
        transparent.add(Material.WALL_SIGN);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}