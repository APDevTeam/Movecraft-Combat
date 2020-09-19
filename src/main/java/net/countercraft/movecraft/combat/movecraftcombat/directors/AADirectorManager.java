package net.countercraft.movecraft.combat.movecraftcombat.directors;

import net.countercraft.movecraft.craft.CraftManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public class AADirectorManager extends DirectorManager {
    private long lastCheck = 0;

    public void run() {
        long ticksElapsed = (System.currentTimeMillis() - lastCheck) / 50;
        if (ticksElapsed <= 3) {
            return;
        }

        processDirectors();
        lastCheck = System.currentTimeMillis();
    }

    private void processDirectors() {
        for (World w : Bukkit.getWorlds()) {
            if (w == null)
                continue;
            for (SmallFireball fireball : w.getEntitiesByClass(SmallFireball.class)) {
                if (!(fireball.getShooter() instanceof org.bukkit.entity.LivingEntity)
                        && w.getPlayers().size() > 0) {
                    Craft c = CraftManager.getInstance().fastNearestCraftToLoc(fireball.getLocation());
                    Player p = null;
                    if (c == null)
                        continue;
                    MovecraftLocation midPoint = c.getHitBox().getMidPoint();
                    int distX = Math.abs(midPoint.getX() - fireball.getLocation().getBlockX());
                    int distY = Math.abs(midPoint.getY() - fireball.getLocation().getBlockY());
                    int distZ = Math.abs(midPoint.getZ() - fireball.getLocation().getBlockZ());
                    boolean inRange = (distX < Config.AADirectorDistance) && (distY < Config.AADirectorDistance) && (distZ < Config.AADirectorDistance);
                    if (hasDirector(c) && inRange) {
                        p = getDirector(c);
                        if (p.getInventory().getItemInMainHand().getType() == Config.DirectorTool) {
                            Vector fv = fireball.getVelocity();
                            double speed = fv.length(); // store the speed to add it back in later, since all the values we will be using are "normalized", IE: have a speed of 1
                            fv = fv.normalize(); // you normalize it for comparison with the new direction to see if we are trying to steer too far
                            Block targetBlock = p.getTargetBlock(Config.Transparent, Config.AADirectorRange);
                            Vector targetVector;
                            if (targetBlock == null) { // the player is looking at nothing, shoot in that general direction
                                targetVector = p.getLocation().getDirection();
                            } else { // shoot directly at the block the player is looking at (IE: with convergence)
                                targetVector = targetBlock.getLocation().toVector().subtract(fireball.getLocation().toVector());
                                targetVector = targetVector.normalize();
                            }
                            if (targetVector.getX() - fv.getX() > 0.5) {
                                fv.setX(fv.getX() + 0.5);
                            } else if (targetVector.getX() - fv.getX() < -0.5) {
                                fv.setX(fv.getX() - 0.5);
                            } else {
                                fv.setX(targetVector.getX());
                            }
                            if (targetVector.getY() - fv.getY() > 0.5) {
                                fv.setY(fv.getY() + 0.5);
                            } else if (targetVector.getY() - fv.getY() < -0.5) {
                                fv.setY(fv.getY() - 0.5);
                            } else {
                                fv.setY(targetVector.getY());
                            }
                            if (targetVector.getZ() - fv.getZ() > 0.5) {
                                fv.setZ(fv.getZ() + 0.5);
                            } else if (targetVector.getZ() - fv.getZ() < -0.5) {
                                fv.setZ(fv.getZ() - 0.5);
                            } else {
                                fv.setZ(targetVector.getZ());
                            }
                            fv = fv.multiply(speed); // put the original speed back in, but now along a different trajectory
                            fireball.setVelocity(fv);
                            fireball.setDirection(fv);
                        }
                    } else if (inRange) {
                        p = c.getNotificationPlayer();
                    }
                    if (p != null)
                        fireball.setShooter(p);
                }
            }
        }
    }
}
