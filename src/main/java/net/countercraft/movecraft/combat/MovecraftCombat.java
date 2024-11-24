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
        I18nSupport.load(getConfig());

        CombatRelease.load(getConfig());

        Directors.load(getConfig());
        AADirectors.load(getConfig());
        ArrowDirectors.load(getConfig());
        CannonDirectors.load(getConfig());

        MovementTracers.load(getConfig());
        TNTTracers.load(getConfig());

        DamageTracking.load(getConfig());

        AddFiresToHitbox.load(getConfig());
        AntiRadar.load(getConfig());
        ContactExplosives.load(getConfig());
        DurabilityOverride.load(getConfig());
        FireballLifespan.load(getConfig());
        FireballPenetration.load(getConfig());
        ReImplementTNTTranslocation.load(getConfig());
        BlockBehaviorOverride.load(getConfig());

        // Register event translation listeners
        getServer().getPluginManager().registerEvents(new CraftCollisionExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);

        // Register features
        var combatRelease = new CombatRelease();
        getServer().getPluginManager().registerEvents(combatRelease, this);
        combatRelease.runTaskTimer(this, 0, 200); // Every 10 seconds

        var aaDirectors = new AADirectors();
        getServer().getPluginManager().registerEvents(aaDirectors, this);
        aaDirectors.runTaskTimer(this, 0, 1); // Every tick
        var arrowDirectors = new ArrowDirectors();
        getServer().getPluginManager().registerEvents(arrowDirectors, this);
        arrowDirectors.runTaskTimer(this, 0, 1); // Every tick
        var cannonDirectors = new CannonDirectors();
        getServer().getPluginManager().registerEvents(cannonDirectors, this);
        cannonDirectors.runTaskTimer(this, 0, 1); // Every tick

        var playerManager = new PlayerManager();
        getServer().getPluginManager().registerEvents(playerManager, this);
        getServer().getPluginManager().registerEvents(new MovementTracers(playerManager), this);
        var tntTracers = new TNTTracers(playerManager);
        getServer().getPluginManager().registerEvents(tntTracers, this);
        tntTracers.runTaskTimer(this, 0, 1); // Every tick

        var damageTracking = new DamageTracking();
        getServer().getPluginManager().registerEvents(damageTracking, this);
        getServer().getPluginManager().registerEvents(new FireballTracking(damageTracking, aaDirectors), this);
        getServer().getPluginManager().registerEvents(new TNTTracking(damageTracking, cannonDirectors), this);


        getServer().getPluginManager().registerEvents(new AddFiresToHitbox(), this);
        getServer().getPluginManager().registerEvents(new AntiRadar(), this);
        var contactExplosives = new ContactExplosives();
        getServer().getPluginManager().registerEvents(contactExplosives, this);
        contactExplosives.runTaskTimer(this, 0, 1); // Every tick
        getServer().getPluginManager().registerEvents(new DurabilityOverride(), this);
        var fireballLifespan = new FireballLifespan();
        getServer().getPluginManager().registerEvents(fireballLifespan, this);
        fireballLifespan.runTaskTimer(this, 0, 20); // Every 1 second
        getServer().getPluginManager().registerEvents(new FireballLifespan(), this);
        getServer().getPluginManager().registerEvents(new FireballPenetration(), this);
        getServer().getPluginManager().registerEvents(new ReImplementTNTTranslocation(), this);


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
