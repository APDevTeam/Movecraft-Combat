package net.countercraft.movecraft.combat;

import net.countercraft.movecraft.combat.features.*;
import net.countercraft.movecraft.combat.features.combat.CombatRelease;
import net.countercraft.movecraft.combat.features.directors.AADirectors;
import net.countercraft.movecraft.combat.features.directors.ArrowDirectors;
import net.countercraft.movecraft.combat.features.directors.CannonDirectors;
import net.countercraft.movecraft.combat.features.directors.Directors;
import net.countercraft.movecraft.combat.features.tracers.MovementTracers;
import net.countercraft.movecraft.combat.features.tracers.TNTTracers;
import net.countercraft.movecraft.combat.features.tracers.commands.MovementTracerSettingCommand;
import net.countercraft.movecraft.combat.features.tracers.commands.TNTTracerModeCommand;
import net.countercraft.movecraft.combat.features.tracers.commands.TNTTracerSettingCommand;
import net.countercraft.movecraft.combat.features.tracers.config.PlayerManager;
import net.countercraft.movecraft.combat.features.BlockBehaviorOverride;
import net.countercraft.movecraft.combat.features.tracking.DamageTracking;
import net.countercraft.movecraft.combat.features.tracking.FireballTracking;
import net.countercraft.movecraft.combat.features.tracking.TNTTracking;
import net.countercraft.movecraft.combat.listener.CraftCollisionExplosionListener;
import net.countercraft.movecraft.combat.listener.ExplosionListener;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;


public final class MovecraftCombat extends JavaPlugin {
    private static MovecraftCombat instance = null;

    public static MovecraftCombat getInstance() {
        return instance;
    }


    @Override
    public void onLoad() {
        AADirectors.register();
        ArrowDirectors.register();
        CannonDirectors.register();
        MovementTracers.register();
    }

    @Override
    public void onEnable() {
        instance = this;


        // Save default config, create default userdata and language if needed
        saveDefaultConfig();

        File folder = new File(getDataFolder(), "userdata");
        if (!folder.exists()) {
            getLogger().info("Created userdata directory");
            folder.mkdirs();
        }

        String[] languages = {"en", "no"};
        for (String s : languages) {
            if (!new File(getDataFolder() + "/localisation/mcclang_" + s + ".properties").exists()) {
                saveResource("localisation/mcclang_" + s + ".properties", false);
            }
        }


        // Load localisation and features from config
        FileConfiguration config = getConfig();
        I18nSupport.load(config);

        CombatRelease.load(config);

        Directors.load(config);
        AADirectors.load(config);
        ArrowDirectors.load(config);
        CannonDirectors.load(config);

        MovementTracers.load(config);
        TNTTracers.load(config);

        DamageTracking.load(config);

        AddFiresToHitbox.load(config);
        AntiRadar.load(config);
        ContactExplosives.load(config);
        DurabilityOverride.load(config);
        FireballBehaviour.load(config);
        FireballPenetration.load(config);
        ReImplementTNTTranslocation.load(config);
        BlockBehaviorOverride.load(config);
        
        // Register event translation listeners
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new CraftCollisionExplosionListener(), this);
        pluginManager.registerEvents(new ExplosionListener(), this);


        // Register features
        var combatRelease = new CombatRelease();
        pluginManager.registerEvents(combatRelease, this);
        combatRelease.runTaskTimer(this, 0, 200); // Every 10 seconds

        var aaDirectors = new AADirectors();
        pluginManager.registerEvents(aaDirectors, this);
        aaDirectors.runTaskTimer(this, 0, 1); // Every tick
        var arrowDirectors = new ArrowDirectors();
        pluginManager.registerEvents(arrowDirectors, this);
        arrowDirectors.runTaskTimer(this, 0, 1); // Every tick
        var cannonDirectors = new CannonDirectors();
        pluginManager.registerEvents(cannonDirectors, this);
        cannonDirectors.runTaskTimer(this, 0, 1); // Every tick

        var playerManager = new PlayerManager();
        pluginManager.registerEvents(playerManager, this);
        pluginManager.registerEvents(new MovementTracers(playerManager), this);
        var tntTracers = new TNTTracers(playerManager);
        pluginManager.registerEvents(tntTracers, this);
        tntTracers.runTaskTimer(this, 0, 1); // Every tick

        var damageTracking = new DamageTracking();
        pluginManager.registerEvents(damageTracking, this);
        pluginManager.registerEvents(new FireballTracking(damageTracking, aaDirectors), this);
        pluginManager.registerEvents(new TNTTracking(damageTracking, cannonDirectors), this);


        pluginManager.registerEvents(new AddFiresToHitbox(), this);
        pluginManager.registerEvents(new AntiRadar(), this);
        var contactExplosives = new ContactExplosives();
        pluginManager.registerEvents(contactExplosives, this);
        contactExplosives.runTaskTimer(this, 0, 1); // Every tick
        pluginManager.registerEvents(new DurabilityOverride(), this);
        var fireballBehavior = new FireballBehaviour();
        pluginManager.registerEvents(fireballBehavior, this);
        fireballBehavior.runTaskTimer(this, 0, 20); // Every 1 second
        pluginManager.registerEvents(new FireballPenetration(), this);
        pluginManager.registerEvents(new ReImplementTNTTranslocation(), this);

        if (config.contains("BleedfixExplosion") && config.getBoolean("BleedfixExplosion")) {
            // don't bother registering the event if the feature is disabled
            pluginManager.registerEvents(new Bleedfix(), this);
        }

        // Register commands
        getCommand("tnttracersetting").setExecutor(new TNTTracerSettingCommand(playerManager));
        getCommand("tnttracermode").setExecutor(new TNTTracerModeCommand(playerManager));
        getCommand("movementtracersetting").setExecutor(new MovementTracerSettingCommand(playerManager));

        // Modify blast resistances
        BlockBehaviorOverride.enable();
    }

    @Override
    public void onDisable() {
        BlockBehaviorOverride.disable(); // Revert to vanilla
    }
}
