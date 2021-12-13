package net.countercraft.movecraft.combat.features.tracers;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.features.tracers.config.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TNTTracers extends BukkitRunnable implements Listener {
    public static double TracerRateTicks = 5.0;
    public static long TracerMinDistanceSqrd = 360;
    public static Particle TracerParticle = null;
    public static Particle ExplosionParticle = null;

    public static void load(@NotNull FileConfiguration config) {
        TracerRateTicks = config.getDouble("TracerRateTicks", 5.0);
        TracerMinDistanceSqrd = config.getLong("TracerMinDistance", 60);
        TracerMinDistanceSqrd *= TracerMinDistanceSqrd;
        TracerParticle = Particle.valueOf(config.getString("TracerParticles", "FIREWORKS_SPARK"));
        ExplosionParticle = Particle.valueOf(config.getString("ExplosionParticles", "VILLAGER_ANGRY"));
    }


    @NotNull
    private final PlayerManager manager;
    private long lastUpdate = 0;

    public TNTTracers(@NotNull PlayerManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        if(TracerRateTicks == 0)
            return;

        long ticksElapsed = (System.currentTimeMillis() - lastUpdate) / 50;
        if(ticksElapsed < TracerRateTicks)
            return;

        long maxDistSquared = Bukkit.getServer().getViewDistance() * 16L;
        maxDistSquared = maxDistSquared - 16;
        maxDistSquared = maxDistSquared * maxDistSquared;

        for(World w : Bukkit.getWorlds()) {
            if(w == null)
                continue;

            for(TNTPrimed tnt : w.getEntitiesByClass(TNTPrimed.class)) {
                if (tnt.getVelocity().lengthSquared() < 0.25)
                    continue;

                final Location tntLoc = tnt.getLocation();
                for(Player p : w.getPlayers()) {
                    String setting = manager.getSetting(p);
                    if(setting == null || setting.equals("OFF") || setting.equals("LOW"))
                        continue;
                    else if(setting.equals("MEDIUM")) {
                        long seed = (long) (tntLoc.getX() * tntLoc.getY() * tntLoc.getZ() + (System.currentTimeMillis() >> 12));
                        int random = new Random(seed).nextInt(100);
                        if(random < 50)
                            continue;   // Medium merely spawns half the particles/cobwebs
                    }

                    // is the TNT within the view distance (rendered world) of the player?
                    if(p.getLocation().distanceSquared(tntLoc) > maxDistSquared)
                        continue;

                    final Player fp = p;
                    String mode = manager.getMode(p);
                    if(mode == null)
                        return;

                    switch(mode) {
                        case "PARTICLES":
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    fp.spawnParticle(TracerParticle, tntLoc, 0, 0.0, 0.0, 0.0);
                                }
                            }.runTaskLater(MovecraftCombat.getInstance(), 5);
                            break;
                        case "BLOCKS":
                        default:
                            // then make a cobweb to look like smoke,
                            // place it a little later so it isn't right
                            // in the middle of the volley
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    fp.sendBlockChange(tntLoc, Material.COBWEB.createBlockData());
                                }
                            }.runTaskLater(MovecraftCombat.getInstance(), 5);
                            // then restore it
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    fp.sendBlockChange(tntLoc, tntLoc.getBlock().getBlockData());
                                }
                            }.runTaskLater(MovecraftCombat.getInstance(), 160);
                            break;
                    }
                }
            }
        }
        lastUpdate = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityExplodeEvent(@NotNull EntityExplodeEvent e) {
        Entity tnt = e.getEntity();
        if(e.getEntityType() != EntityType.PRIMED_TNT)
            return;
        if(TracerRateTicks == 0)
            return;

        long maxDistSquared = Bukkit.getServer().getViewDistance() * 16L;
        maxDistSquared = maxDistSquared - 16;
        maxDistSquared = maxDistSquared * maxDistSquared;

        for(Player p : e.getEntity().getWorld().getPlayers()) {
            String setting = manager.getSetting(p);
            if(setting == null || setting.equals("OFF"))
                continue;

            // is the TNT within the view distance (rendered world) of the player, yet further than TracerMinDistance blocks?
            double distance = p.getLocation().distanceSquared(tnt.getLocation());
            if(distance >= maxDistSquared || distance < TracerMinDistanceSqrd)
                return;

            final Location loc = tnt.getLocation();
            final Player fp = p;

            String mode = manager.getMode(p);
            if(mode == null)
                continue;

            if(mode.equals("BLOCKS")) {
                // then make a glowstone to look like the explosion, place it a little later so it isn't right in the middle of the volley
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        fp.sendBlockChange(loc, Material.GLOWSTONE.createBlockData());
                    }
                }.runTaskLater(MovecraftCombat.getInstance(), 5);
                // then remove it
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        fp.sendBlockChange(loc, Material.AIR.createBlockData());
                    }
                }.runTaskLater(MovecraftCombat.getInstance(), 160);
            }
            else if(mode.equals("PARTICLES")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        fp.spawnParticle(ExplosionParticle, loc, 9);
                    }
                }.runTaskLater(MovecraftCombat.getInstance(), 20);
            }
        }
    }
}
