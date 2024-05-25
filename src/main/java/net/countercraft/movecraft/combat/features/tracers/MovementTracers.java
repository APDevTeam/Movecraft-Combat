package net.countercraft.movecraft.combat.features.tracers;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.features.tracers.config.PlayerConfig;
import net.countercraft.movecraft.combat.features.tracers.config.PlayerManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.MaterialSetProperty;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashSet;

public class MovementTracers implements Listener {
    public static final NamespacedKey MOVEMENT_TRACER_BLOCKS = new NamespacedKey("movecraft-combat", "movement_tracer_blocks");
    public static boolean MovementTracers = false;
    public static Particle SpecificParticle = null;
    public static Particle GeneralParticle = null;
    @NotNull
    private final PlayerManager manager;


    public MovementTracers(@NotNull PlayerManager manager) {
        this.manager = manager;
    }

    public static void register() {
        CraftType.registerProperty(new MaterialSetProperty("movementTracerBlocks", MOVEMENT_TRACER_BLOCKS, type -> {
            var materials = EnumSet.noneOf(Material.class);
            var moveBlocks = type.getRequiredBlockProperty(CraftType.MOVE_BLOCKS);
            for (var entry : moveBlocks) {
                materials.addAll(entry.getMaterials());
            }
            return materials;
        }));
    }

    public static void load(@NotNull FileConfiguration config) {
        MovementTracers = config.getBoolean("MovementTracers", false);
        SpecificParticle = Particle.valueOf(config.getString("SpecificParticle", "COMPOSTER"));
        GeneralParticle = Particle.valueOf(config.getString("GeneralParticle", "CRIT"));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCraftTranslate(CraftTranslateEvent e) {
        if (!MovementTracers)
            return;
        if (e.getNewHitBox().isEmpty() || e.getOldHitBox().isEmpty())
            return;

        World w = e.getCraft().getWorld();
        // difference between old and new hitbox
        final HitBox difference = e.getOldHitBox().difference(e.getNewHitBox());

        final MovecraftLocation delta = e.getNewHitBox().getMidPoint().subtract(e.getOldHitBox().getMidPoint());
        var materials = e.getCraft().getType().getMaterialSetProperty(MOVEMENT_TRACER_BLOCKS);

        final HashSet<Location> specificLocations = new HashSet<>();
        final HashSet<Location> generalLocations = new HashSet<>();
        for (MovecraftLocation movecraftLocation : difference) {
            Location spawnLoc = movecraftLocation.toBukkit(w).add(0.5, 0.5, 0.5);
            if (materials.contains(movecraftLocation.toBukkit(w).getBlock().getType())) {
                // Add to special locations if the block is a movement tracer block
                specificLocations.add(spawnLoc);
            }
            else {
                // Else add to normal locations
                generalLocations.add(spawnLoc);
            }
        }

        long maxDistSquared = Bukkit.getServer().getViewDistance() * 16L;
        maxDistSquared = maxDistSquared - 16;
        maxDistSquared = maxDistSquared * maxDistSquared;
        Location center = e.getNewHitBox().getMidPoint().toBukkit(w);

        for (final Player p : e.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(center) > maxDistSquared)
                continue;
            PlayerConfig.MovementSetting setting = manager.getMovementSetting(p);
            if (setting == PlayerConfig.MovementSetting.OFF)
                continue;

            // Display specific locations
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (var loc : specificLocations)
                        p.spawnParticle(SpecificParticle, loc, 0, 0.0, 0.0, 0.0);
                }
            }.runTaskLater(MovecraftCombat.getInstance(), 1);

            // Display general locations a tick later
            if(setting == PlayerConfig.MovementSetting.HIGH) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (var loc : generalLocations)
                            p.spawnParticle(GeneralParticle, loc, 0, 0.0, 0.0, 0.0);
                    }
                }.runTaskLater(MovecraftCombat.getInstance(), 2);
            }
        }
    }
}
