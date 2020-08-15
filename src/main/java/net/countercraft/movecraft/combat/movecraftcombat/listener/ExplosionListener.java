package net.countercraft.movecraft.combat.movecraftcombat.listener;

import java.util.Random;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.craft.CraftManager;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.TNTTracking;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.FireballTracking;


public class ExplosionListener implements Listener {
    @EventHandler
    public void entityExplodeEvent(EntityExplodeEvent e) {
        processDurabilityOverride(e);
        processTracers(e);
        processTNTTracking(e);
        processFireballTracking(e);
    }


    private void processDurabilityOverride(@NotNull EntityExplodeEvent e) {
        if (e.getEntity() == null)
            return;
        if (e.getEntityType() != EntityType.PRIMED_TNT)
            return;
        if(Config.DurabilityOverride == null)
            return;

        // Sorry for the following monster conditional statement, it is necessary to avoid spalling.
        // Basically it runs a random number based on the XYZ of the block and the system time if the block has explosion resistance
        // And then it also removes the block if no adjacent blocks are air (IE: the explosion skipped a block)
        e.blockList().removeIf(b -> (Config.DurabilityOverride.containsKey(b.getTypeId()) &&
                (new Random( b.getX()*b.getY()*b.getZ()+(System.currentTimeMillis() >> 12)).nextInt(100) < Config.DurabilityOverride.get(b.getTypeId()))) ||
                !(b.getRelative(BlockFace.EAST).isEmpty() || b.getRelative(BlockFace.WEST).isEmpty() || b.getRelative(BlockFace.UP).isEmpty() ||
                        b.getRelative(BlockFace.NORTH).isEmpty() || b.getRelative(BlockFace.SOUTH).isEmpty() || b.getRelative(BlockFace.DOWN).isEmpty()));
//                    (new Random( new Random(b.getX()).nextInt(100)+ new Random(b.getY()).nextInt(100) + new Random(b.getZ()).nextInt(100)+
    }

    private void processTracers(@NotNull EntityExplodeEvent e) {
        if (e.getEntity() == null)
            return;
        Entity tnt = e.getEntity();
        if (e.getEntityType() == EntityType.PRIMED_TNT && Config.TracerRateTicks != 0) {
            long maxDistSquared = Bukkit.getServer().getViewDistance() * 16;
            maxDistSquared = maxDistSquared - 16;
            maxDistSquared = maxDistSquared * maxDistSquared;

            for (Player p : e.getEntity().getWorld().getPlayers()) {
                String setting = MovecraftCombat.getInstance().getPlayerManager().getSetting(p);
                if(setting == null || setting.equals("OFF")) {
                    continue;
                }

                // is the TNT within the view distance (rendered world) of the player, yet further than TracerMinDistance blocks?
                if (p.getLocation().distanceSquared(tnt.getLocation()) < maxDistSquared && p.getLocation().distanceSquared(tnt.getLocation()) >= Config.TracerMinDistanceSqrd) {  // we use squared because its faster
                    final Location loc = tnt.getLocation();
                    final Player fp = p;
                    final World fw = e.getEntity().getWorld();

                    String mode = MovecraftCombat.getInstance().getPlayerManager().getMode(p);
                    if (mode.equals("BLOCKS")) {
                        // then make a glowstone to look like the explosion, place it a little later so it isn't right in the middle of the volley
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                fp.sendBlockChange(loc, 89, (byte) 0);
                            }
                        }.runTaskLater(Movecraft.getInstance(), 5);
                        // then remove it
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                fp.sendBlockChange(loc, 0, (byte) 0);
                            }
                        }.runTaskLater(Movecraft.getInstance(), 160);
                    }
                    else if (mode.equals("PARTICLES")) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                fp.spawnParticle(Particle.VILLAGER_ANGRY, loc, 9);
                            }
                        }.runTaskLater(Movecraft.getInstance(), 20);
                    }
                }
            }
        }
    }

    private void processTNTTracking(@NotNull EntityExplodeEvent e) {
        if (e.getEntity() == null)
            return;
        if (e.getEntityType() != EntityType.PRIMED_TNT)
            return;

        TNTPrimed tnt = (TNTPrimed) e.getEntity();
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getLocation());
        if(craft == null)
            return;
        for(Block b : e.blockList()) {
            if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation()))) {
                TNTTracking.getInstance().damagedCraft(craft, tnt);
                return;
            }
        }
        TNTTracking.getInstance().explodedTNT(tnt);
    }

    private void processFireballTracking(@NotNull EntityExplodeEvent e) {
        if(!Config.EnableFireballTracking)
            return;
        if(e.getEntity() == null)
            return;
        if(!(e.getEntity() instanceof Fireball))
            return;
        Fireball fireball = (Fireball) e.getEntity();
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getLocation());
        if(craft == null)
            return;
        if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getLocation()))) {
            FireballTracking.getInstance().damagedCraft(craft, fireball);
            return;
        }
        for(Block b : e.blockList()) {
            if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation()))) {
                FireballTracking.getInstance().damagedCraft(craft, fireball);
                return;
            }
        }
        FireballTracking.getInstance().expiredFireball(fireball);
    }
}
