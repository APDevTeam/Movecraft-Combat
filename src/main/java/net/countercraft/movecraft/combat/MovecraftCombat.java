package net.countercraft.movecraft.combat;

import net.countercraft.movecraft.combat.commands.TracerModeCommand;
import net.countercraft.movecraft.combat.commands.TracerSettingCommand;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.features.AADirectors;
import net.countercraft.movecraft.combat.features.AddFiresToHitbox;
import net.countercraft.movecraft.combat.features.AntiRadar;
import net.countercraft.movecraft.combat.features.CannonDirectors;
import net.countercraft.movecraft.combat.features.Directors;
import net.countercraft.movecraft.combat.features.DurabilityOverride;
import net.countercraft.movecraft.combat.features.FireballLifespan;
import net.countercraft.movecraft.combat.features.FireballPenetration;
import net.countercraft.movecraft.combat.features.MovementTracers;
import net.countercraft.movecraft.combat.features.ReImplementTNTTranslocation;
import net.countercraft.movecraft.combat.features.TNTTracers;
import net.countercraft.movecraft.combat.features.damagetracking.DamageManager;
import net.countercraft.movecraft.combat.listener.*;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.player.PlayerManager;
import net.countercraft.movecraft.combat.status.StatusManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;


public final class MovecraftCombat extends JavaPlugin {
    private static MovecraftCombat instance = null;

    @Deprecated(forRemoval = true)
    private PlayerManager playerManager;

    public static MovecraftCombat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        saveDefaultConfig();


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

        Config.EnableContactExplosives = getConfig().getBoolean("EnableContactExplosives", true);
        Config.CannonDirectorDistance = getConfig().getInt("CannonDirectorsDistance", 100);
        Config.CannonDirectorRange = getConfig().getInt("CannonDirectorRange", 120);
        Config.ContactExplosivesMaxImpulseFactor = getConfig().getDouble("ContactExplosivesMaxImpulseFactor", 10.0);

        AddFiresToHitbox.load(getConfig());
        AntiRadar.load(getConfig());
        Directors.load(getConfig());
        DurabilityOverride.load(getConfig());
        FireballLifespan.load(getConfig());
        FireballPenetration.load(getConfig());
        MovementTracers.load(getConfig());
        ReImplementTNTTranslocation.load(getConfig());
        TNTTracers.load(getConfig());


        Config.EnableFireballTracking = getConfig().getBoolean("EnableFireballTracking", false);
        Config.EnableTNTTracking = getConfig().getBoolean("EnableTNTTracking", true);
        Config.EnableTorpedoTracking = getConfig().getBoolean("EnableTorpedoTracking", false);
        Config.DamageTimeout = getConfig().getInt("DamageTimeout", 300);
        Config.EnableCombatReleaseTracking = getConfig().getBoolean("EnableCombatReleaseTracking", false);
        Config.EnableCombatReleaseKick = getConfig().getBoolean("EnableCombatReleaseKick", true);
        Config.CombatReleaseBanLength = getConfig().getLong("CombatReleaseBanLength", 60);
        Config.CombatReleaseScuttle = getConfig().getBoolean("CombatReleaseScuttle", true);

        getCommand("tracersetting").setExecutor(new TracerSettingCommand());
        getCommand("tracermode").setExecutor(new TracerModeCommand());

        getServer().getPluginManager().registerEvents(new CraftCollisionExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new CraftReleaseListener(), this);
        getServer().getPluginManager().registerEvents(new CraftScuttleListener(), this);
        getServer().getPluginManager().registerEvents(new DispenseListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(), this);

        getServer().getPluginManager().registerEvents(new DamageManager(), this);
        var aaDirectors = new AADirectors();
        getServer().getPluginManager().registerEvents(aaDirectors, this);
        aaDirectors.runTaskTimer(this, 0, 1);
        getServer().getPluginManager().registerEvents(new AddFiresToHitbox(), this);
        getServer().getPluginManager().registerEvents(new AntiRadar(), this);
        var cannonDirectors = new CannonDirectors();
        getServer().getPluginManager().registerEvents(cannonDirectors, this);
        cannonDirectors.runTaskTimer(this, 0, 1);
        getServer().getPluginManager().registerEvents(new DurabilityOverride(), this);
        getServer().getPluginManager().registerEvents(new FireballLifespan(), this);
        getServer().getPluginManager().registerEvents(new FireballPenetration(), this);
        getServer().getPluginManager().registerEvents(new MovementTracers(), this);
        getServer().getPluginManager().registerEvents(new ReImplementTNTTranslocation(), this);
        var tntTracers = new TNTTracers();
        getServer().getPluginManager().registerEvents(tntTracers, this);
        tntTracers.runTaskTimer(this, 0, 1);

        playerManager = new PlayerManager();

        StatusManager statusTracking = new StatusManager();
        statusTracking.runTaskTimer(this, 0, 200);      // Every 10 seconds
        FireballLifespan fireballLifespan = new FireballLifespan();
        fireballLifespan.runTaskTimer(this, 0, 20);      // Every 1 second
    }

    @Override
    public void onDisable() {
        playerManager.shutDown();
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
