package net.countercraft.movecraft.combat;

import net.countercraft.movecraft.combat.commands.TracerModeCommand;
import net.countercraft.movecraft.combat.commands.TracerSettingCommand;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.features.AADirectors;
import net.countercraft.movecraft.combat.features.AddFiresToHitbox;
import net.countercraft.movecraft.combat.features.AntiRadar;
import net.countercraft.movecraft.combat.features.CannonDirectors;
import net.countercraft.movecraft.combat.features.CombatRelease;
import net.countercraft.movecraft.combat.features.ContactExplosives;
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

        DamageTracking.load(getConfig());

        AADirectors.load(getConfig());
        AddFiresToHitbox.load(getConfig());
        AntiRadar.load(getConfig());
        CannonDirectors.load(getConfig());
        CombatRelease.load(getConfig());
        ContactExplosives.load(getConfig());
        Directors.load(getConfig());
        DurabilityOverride.load(getConfig());
        FireballLifespan.load(getConfig());
        FireballPenetration.load(getConfig());
        MovementTracers.load(getConfig());
        ReImplementTNTTranslocation.load(getConfig());
        TNTTracers.load(getConfig());

        getCommand("tracersetting").setExecutor(new TracerSettingCommand());
        getCommand("tracermode").setExecutor(new TracerModeCommand());

        getServer().getPluginManager().registerEvents(new CraftCollisionExplosionListener(), this);


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
        aaDirectors.runTaskTimer(this, 0, 1); // Every tick

        getServer().getPluginManager().registerEvents(new AddFiresToHitbox(), this);
        getServer().getPluginManager().registerEvents(new AntiRadar(), this);

        var combatRelease = new CombatRelease();
        getServer().getPluginManager().registerEvents(combatRelease, this);
        combatRelease.runTaskTimer(this, 0, 200); // Every 10 seconds

        var cannonDirectors = new CannonDirectors();
        getServer().getPluginManager().registerEvents(cannonDirectors, this);
        cannonDirectors.runTaskTimer(this, 0, 1); // Every tick

        var contactExplosives = new ContactExplosives();
        getServer().getPluginManager().registerEvents(contactExplosives, this);
        contactExplosives.runTaskTimer(this, 0, 1); // Every tick

        getServer().getPluginManager().registerEvents(new DurabilityOverride(), this);

        new FireballLifespan().runTaskTimer(this, 0, 20); // Every 1 second

        getServer().getPluginManager().registerEvents(new FireballLifespan(), this);
        getServer().getPluginManager().registerEvents(new FireballPenetration(), this);
        getServer().getPluginManager().registerEvents(new MovementTracers(), this);
        getServer().getPluginManager().registerEvents(new ReImplementTNTTranslocation(), this);

        var tntTracers = new TNTTracers();
        getServer().getPluginManager().registerEvents(tntTracers, this);
        tntTracers.runTaskTimer(this, 0, 1); // Every tick

        playerManager = new PlayerManager();
    }

    @Override
    public void onDisable() {
        playerManager.shutDown();
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
