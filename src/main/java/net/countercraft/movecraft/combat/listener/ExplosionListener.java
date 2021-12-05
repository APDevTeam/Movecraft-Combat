package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.directors.CannonDirectorManager;
import net.countercraft.movecraft.combat.tracking.FireballTracking;
import net.countercraft.movecraft.combat.tracking.TNTTracking;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;


public class ExplosionListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityExplodeEvent(EntityExplodeEvent e) {
        processTracers(e);
        processTNTTracking(e);
        processFireballTracking(e);
        processDirectors(e);
    }


    private void processTracers(@NotNull EntityExplodeEvent e) {
        Entity tnt = e.getEntity();
        if (e.getEntityType() == EntityType.PRIMED_TNT && Config.TracerRateTicks != 0) {
            long maxDistSquared = Bukkit.getServer().getViewDistance() * 16L;
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

                    String mode = MovecraftCombat.getInstance().getPlayerManager().getMode(p);
                    if(mode == null)
                        continue;
                    if (mode.equals("BLOCKS")) {
                        // then make a glowstone to look like the explosion, place it a little later so it isn't right in the middle of the volley
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                fp.sendBlockChange(loc, Material.GLOWSTONE, (byte) 0);
                            }
                        }.runTaskLater(MovecraftCombat.getInstance(), 5);
                        // then remove it
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                fp.sendBlockChange(loc, Material.AIR, (byte) 0);
                            }
                        }.runTaskLater(MovecraftCombat.getInstance(), 160);
                    }
                    else if (mode.equals("PARTICLES")) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                fp.spawnParticle(Config.ExplosionParticle, loc, 9);
                            }
                        }.runTaskLater(MovecraftCombat.getInstance(), 20);
                    }
                }
            }
        }
    }

    private void processTNTTracking(@NotNull EntityExplodeEvent e) {
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
    }

    private void processFireballTracking(@NotNull EntityExplodeEvent e) {
        if(!Config.EnableFireballTracking)
            return;
        if(!(e.getEntity() instanceof Fireball))
            return;
        Fireball fireball = (Fireball) e.getEntity();
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getLocation());
        if(!(craft instanceof PlayerCraft))
            return;
        PlayerCraft playerCraft = (PlayerCraft) craft;
        if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getLocation()))) {
            FireballTracking.getInstance().damagedCraft(playerCraft, fireball);
            return;
        }
        for(Block b : e.blockList()) {
            if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation()))) {
                FireballTracking.getInstance().damagedCraft(playerCraft, fireball);
                return;
            }
        }
    }

    private void processDirectors(@NotNull EntityExplodeEvent e) {
        if(e.getEntity() instanceof TNTPrimed) {
            CannonDirectorManager.getInstance().removeTNT((TNTPrimed) e.getEntity());
        }
    }
}
