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
import net.countercraft.movecraft.combat.features.damagetracking.DamageTracking;
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


        File folder = new File(getDataFolder(), "userdata");
        if(!folder.exists()) {
            getLogger().info("Created userdata directory");
            folder.mkdirs();
        }

        String[] languages = {"en", "no"};
        for(String s : languages) {
            if(!new File(getDataFolder() + "/localisation/mcclang_" + s + ".properties").exists()) {
                saveResource("localisation/mcclang_" + s + ".properties", false);
            }
        }
        I18nSupport.load(getConfig());

        AddFiresToHitbox.load(getConfig());
        AntiRadar.load(getConfig());
        Directors.load(getConfig());
        DurabilityOverride.load(getConfig());
        FireballLifespan.load(getConfig());
        FireballPenetration.load(getConfig());
        MovementTracers.load(getConfig());
        ReImplementTNTTranslocation.load(getConfig());
        TNTTracers.load(getConfig());

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

        var damageTracking = new DamageTracking();
        getServer().getPluginManager().registerEvents(damageTracking, this);

        var aaDirectors = new AADirectors();
        getServer().getPluginManager().registerEvents(aaDirectors, this);
        aaDirectors.runTaskTimer(this, 0, 1);
        getServer().getPluginManager().registerEvents(new AddFiresToHitbox(), this);
        getServer().getPluginManager().registerEvents(new AntiRadar(), this);
        var cannonDirectors = new CannonDirectors();
        getServer().getPluginManager().registerEvents(cannonDirectors, this);
        cannonDirectors.runTaskTimer(this, 0, 1);
        getServer().getPluginManager().registerEvents(new DurabilityOverride(), this);
        new FireballLifespan().runTaskTimer(this, 0, 20);      // Every 1 second
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
    }

    @Override
    public void onDisable() {
        playerManager.shutDown();
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
