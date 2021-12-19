package net.countercraft.movecraft.combat.features;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ContactExplosives extends BukkitRunnable implements Listener {
    public static boolean EnableContactExplosives = true;
    public static double ContactExplosivesMaxImpulseFactor = 10;
    public static int ContactExplosivesDelay = 10;

    public static void load(@NotNull FileConfiguration config) {
        EnableContactExplosives = config.getBoolean("EnableContactExplosives", true);
        ContactExplosivesMaxImpulseFactor = config.getDouble("ContactExplosivesMaxImpulseFactor", 10.0);
        ContactExplosivesDelay = 80 - (int) (20 * config.getDouble("ContactExplosivesDelay", 0.5D));
    }



    private final Object2DoubleOpenHashMap<TNTPrimed> tracking = new Object2DoubleOpenHashMap<>();
    private long lastCheck = 0;


    @Override
    public void run() {
        if(!EnableContactExplosives)
            return;

        long ticksElapsed = (System.currentTimeMillis() - lastCheck) / 50;
        if(ticksElapsed <= 0)
            return;

        // see if there is any new rapid moving TNT in the worlds
        for(World w : Bukkit.getWorlds()) {
            if(w == null || w.getPlayers().size() == 0)
                continue;

            var allTNT = w.getEntitiesByClass(TNTPrimed.class);
            for(TNTPrimed tnt : allTNT) {
                if(tnt.getFuseTicks() < ContactExplosivesDelay && !tracking.containsKey(tnt)) {
                    Bukkit.getLogger().info("Putting " + tnt.getLocation() + " : " + tnt.getVelocity().lengthSquared() + " w/ fuse " + tnt.getFuseTicks());
                    tracking.put(tnt, tnt.getVelocity().lengthSquared());
                }
                else if(!tracking.containsKey(tnt))
                    Bukkit.getLogger().info("Skipping " + tnt.getLocation() + " : " + tnt.getVelocity().lengthSquared() + " w/ fuse " + tnt.getFuseTicks());
            }
        }

        // now check to see if any has abruptly changed velocity, and should explode
        for(TNTPrimed tnt : tracking.keySet()) {
            double vel = tnt.getVelocity().lengthSquared();
            if(vel < tracking.getDouble(tnt) / ContactExplosivesMaxImpulseFactor) {
                Bukkit.getLogger().info("Esploding: " + tnt.getLocation() + ": " + vel + " / " + tracking.getDouble(tnt));
                tnt.setVelocity(new Vector(0,0,0)); //freeze it in place to prevent sliding
                tnt.setFuseTicks(0);
            }
            else {
                // update the tracking with the new velocity so gradual
                // changes do not make TNT explode
                tracking.put(tnt, vel);
            }
        }

        // then, removed any exploded or invalid TNT from tracking
        tracking.keySet().removeIf(tnt -> !tnt.isValid() || tnt.getFuseTicks() <= 0);

        lastCheck = System.currentTimeMillis();
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent e) {
        if(!EnableContactExplosives)
            return;
        if(!(e.getEntity() instanceof TNTPrimed))
            return;

        tracking.removeDouble(e.getEntity());
    }
}
