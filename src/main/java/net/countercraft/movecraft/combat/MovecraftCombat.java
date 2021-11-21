package net.countercraft.movecraft.combat;

import net.countercraft.movecraft.combat.commands.TracerModeCommand;
import net.countercraft.movecraft.combat.commands.TracerSettingCommand;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.directors.AADirectorManager;
import net.countercraft.movecraft.combat.directors.CannonDirectorManager;
import net.countercraft.movecraft.combat.fireballs.FireballManager;
import net.countercraft.movecraft.combat.listener.*;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.player.PlayerManager;
import net.countercraft.movecraft.combat.radar.RadarManager;
import net.countercraft.movecraft.combat.sign.AADirectorSign;
import net.countercraft.movecraft.combat.sign.CannonDirectorSign;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.combat.tracking.DamageManager;
import net.countercraft.movecraft.combat.utils.LegacyUtils;
import net.countercraft.movecraft.util.Tags;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


public final class MovecraftCombat extends JavaPlugin {
    private static MovecraftCombat instance = null;

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
            if (!new File(getDataFolder() + "/localisation/mcclang_" + s + ".properties").exists()) {
                this.saveResource("localisation/mcclang_" + s + ".properties", false);
            }
        }
        Config.Locale = getConfig().getString("Locale", "en");
        I18nSupport.init();

        Config.AADirectorDistance = getConfig().getInt("AADirectorDistance", 50);
        Config.AADirectorRange = getConfig().getInt("AADirectorRange", 120);
        Config.EnableContactExplosives = getConfig().getBoolean("EnableContactExplosives", true);
        Config.CannonDirectorDistance = getConfig().getInt("CannonDirectorsDistance", 100);
        Config.CannonDirectorRange = getConfig().getInt("CannonDirectorRange", 120);
        Config.ContactExplosivesMaxImpulseFactor = getConfig().getDouble("ContactExplosivesMaxImpulseFactor", 10.0);

        Object tool = getConfig().get("DirectorTool");
        Material directorTool = null;
        if (tool instanceof String)
            directorTool = Material.getMaterial((String) tool);
        if (directorTool == null)
            getLogger().log(Level.SEVERE, "Failed to load director tool " + tool.toString());
        else
            Config.DirectorTool = directorTool;
        if (getConfig().contains("TransparentBlocks")) {
            for (Object o : getConfig().getList("TransparentBlocks")) {
                if (o instanceof String)
                    Config.Transparent.addAll(Tags.parseMaterials((String) o));
                else
                    getLogger().log(Level.SEVERE, "Failed to load transparent " + o.toString());
            }
        }
        if(getConfig().contains("DurabilityOverride")) {
            Map<String, Object> temp = getConfig().getConfigurationSection("DurabilityOverride").getValues(false);
            Config.DurabilityOverride = new HashMap<>();
            for (Map.Entry<String, Object> entry : temp.entrySet()) {
                EnumSet<Material> materials = Tags.parseMaterials(entry.getKey());
                for(Material m : materials)
                    Config.DurabilityOverride.put(m, (Integer) entry.getValue());
            }
        }
        Config.FireballLifespan = getConfig().getInt("FireballLifespan", 6);
        Config.TracerRateTicks = getConfig().getDouble("TracerRateTicks", 5.0);
        Config.TracerMinDistanceSqrd = getConfig().getLong("TracerMinDistance", 60);
        Config.TracerMinDistanceSqrd *= Config.TracerMinDistanceSqrd;
        Config.TracerParticle = Particle.valueOf(getConfig().getString("TracerParticles", "FIREWORKS_SPARK"));
        Config.ExplosionParticle = Particle.valueOf(getConfig().getString("ExplosionParticles", "VILLAGER_ANGRY"));


        Config.EnableFireballTracking = getConfig().getBoolean("EnableFireballTracking", false);
        Config.EnableTNTTracking = getConfig().getBoolean("EnableTNTTracking", true);
        Config.EnableTorpedoTracking = getConfig().getBoolean("EnableTorpedoTracking", false);
        Config.DamageTimeout = getConfig().getInt("DamageTimeout", 300);
        Config.EnableCombatReleaseTracking = getConfig().getBoolean("EnableCombatReleaseTracking", false);
        Config.EnableCombatReleaseKick = getConfig().getBoolean("EnableCombatReleaseKick", true);
        Config.CombatReleaseBanLength = getConfig().getLong("CombatReleaseBanLength", 60);
        Config.CombatReleaseScuttle = getConfig().getBoolean("CombatReleaseScuttle", true);
        Config.EnableAntiRadar = getConfig().getBoolean("EnableAntiRadar", false);
        Config.EnableFireballPenetration = getConfig().getBoolean("EnableFireballPenetration", false);
        Config.AddFiresToHitbox = getConfig().getBoolean("AddFiresToHitbox", true);

        new LegacyUtils();

        if(LegacyUtils.getInstance().isPostTranslocation()) {
            Config.ReImplementTNTTranslocation = getConfig().getBoolean("ReImplementTNTTranslocation", false);

            if(Config.ReImplementTNTTranslocation) {
                getServer().getPluginManager().registerEvents(new PistonListener(), this);
            }
        }

        Config.MovementTracers = getConfig().getBoolean("MovementTracers", false);

        getCommand("tracersetting").setExecutor(new TracerSettingCommand());
        getCommand("tracermode").setExecutor(new TracerModeCommand());

        getServer().getPluginManager().registerEvents(new CraftCollisionExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new CraftDetectListener(), this);
        getServer().getPluginManager().registerEvents(new CraftReleaseListener(), this);
        getServer().getPluginManager().registerEvents(new CraftScuttleListener(), this);
        getServer().getPluginManager().registerEvents(new CraftSinkListener(), this);
        getServer().getPluginManager().registerEvents(new DispenseListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new IgniteListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(), this);
        getServer().getPluginManager().registerEvents(new TranslateListener(), this);
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
        FireballManager fireballManager = new FireballManager();
        fireballManager.runTaskTimer(this, 0, 20);      // Every 1 second
    }

    @Override
    public void onDisable() {
        playerManager.shutDown();
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
