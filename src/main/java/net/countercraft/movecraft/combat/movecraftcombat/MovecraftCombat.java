package net.countercraft.movecraft.combat.movecraftcombat;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

import net.countercraft.movecraft.combat.movecraftcombat.commands.TracerModeCommand;
import net.countercraft.movecraft.combat.movecraftcombat.commands.TracerSettingCommand;
import net.countercraft.movecraft.combat.movecraftcombat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.movecraftcombat.player.PlayerManager;
import net.countercraft.movecraft.combat.movecraftcombat.radar.RadarManager;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.combat.movecraftcombat.sign.*;
import net.countercraft.movecraft.combat.movecraftcombat.listener.*;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageManager;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;
import net.countercraft.movecraft.combat.movecraftcombat.directors.*;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public final class MovecraftCombat extends JavaPlugin {
    private static MovecraftCombat instance;
    private static WorldGuardPlugin wgPlugin;

    private AADirectorManager aaDirectors;
    private CannonDirectorManager cannonDirectors;
    private PlayerManager playerManager;

    public static synchronized MovecraftCombat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        saveDefaultConfig();


        Config.Debug = getConfig().getBoolean("Debug", false);

        File folder = new File(MovecraftCombat.getInstance().getDataFolder(), "userdata");
        if (!folder.exists()) {
            getLogger().info("Created userdata directory");
            folder.mkdirs();
        }

        //TODO other languages
        String[] languages = {"en", "no"};
        for (String s : languages) {
            if (!new File(getDataFolder()  + "/localisation/mcclang_"+ s +".properties").exists()) {
                this.saveResource("localisation/mcclang_"+ s +".properties", false);
            }
        }
        Config.Locale = getConfig().getString("Locale", "en");
        I18nSupport.init();

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
        Object tool = getConfig().get("DirectorTool");
        Material directorTool = null;
        if(tool instanceof String)
            directorTool = Material.getMaterial((String) tool);
        if(directorTool == null)
            getLogger().log(Level.SEVERE, "Failed to load director tool " + tool.toString());
        else
            Config.DirectorTool = directorTool;
        for(Object o : getConfig().getList("TransparentBlocks")) {
            if(o instanceof String)
                Config.Transparent.add(Material.getMaterial(((String) o).toUpperCase()));
            else
                getLogger().log(Level.SEVERE, "Failed to load transparent " + o.toString());
        }
        if(getConfig().contains("DurabilityOverride")) {
            Map<String, Object> temp = getConfig().getConfigurationSection("DurabilityOverride").getValues(false);
            Config.DurabilityOverride = new HashMap<>();
            for (String str : temp.keySet()) {
                Config.DurabilityOverride.put(Material.getMaterial(str.toUpperCase()), (Integer) temp.get(str));
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
        Config.EnableAntiRadar = getConfig().getBoolean("EnableAntiRadar", false);


        Plugin wg = getServer().getPluginManager().getPlugin("WorldGuard");
        if(wg == null || !(wg instanceof WorldGuardPlugin)) {
            getLogger().log(Level.FINE, "Movecraft Combat did not find a compatible version of WorldGuard. Disabling WorldGuard integration.");
            wgPlugin = null;
        }
        else {
            getLogger().log(Level.INFO, "Found a compatible version of WorldGuard. Enabling WorldGuard integration.");
            wgPlugin = (WorldGuardPlugin) wg;
        }

        getCommand("tracersetting").setExecutor(new TracerSettingCommand());
        getCommand("tracermode").setExecutor(new TracerModeCommand());

        getServer().getPluginManager().registerEvents(new CraftCollisionExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new CraftPilotListener(), this);
        getServer().getPluginManager().registerEvents(new CraftReleaseListener(), this);
        getServer().getPluginManager().registerEvents(new CraftScuttleListener(), this);
        getServer().getPluginManager().registerEvents(new CraftSinkListener(), this);
        getServer().getPluginManager().registerEvents(new DispenseListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(), this);
        getServer().getPluginManager().registerEvents(new AADirectorSign(), this);
        getServer().getPluginManager().registerEvents(new CannonDirectorSign(), this);


        aaDirectors = new AADirectorManager();
        aaDirectors.runTaskTimer(this, 0, 1);           // Every tick
        cannonDirectors = new CannonDirectorManager();
        cannonDirectors.runTaskTimer(this, 0, 1);       // Every tick
        playerManager = new PlayerManager();

        DamageManager damageTracking = new DamageManager();
        damageTracking.runTaskTimer(this, 0, 12000);    // Every 10 minutes
        StatusManager statusTracking = new StatusManager();
        statusTracking.runTaskTimer(this, 0, 200);      // Every 10 seconds
        RadarManager radarManager = new RadarManager();
        radarManager.runTaskTimer(this, 0, 12000);      // Every 10 minutes
    }

    @Override
    public void onDisable() {
        playerManager.shutDown();
    }

    public WorldGuardPlugin getWGPlugin() {
        return wgPlugin;
    }

    public CannonDirectorManager getCannonDirectors() {
        return cannonDirectors;
    }

    public AADirectorManager getAADirectors() {
        return aaDirectors;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
