package net.countercraft.movecraft.combat.directors;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.utils.DirectorUtils;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.BooleanProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CannonDirectorManager extends DirectorManager {
    public static final NamespacedKey ALLOW_CANNON_DIRECTOR_SIGN = new NamespacedKey("movecraft-combat", "allow_cannon_director_sign");
    static {
        CraftType.registerProperty(new BooleanProperty("allowCannonDirectorSign", ALLOW_CANNON_DIRECTOR_SIGN, type -> true));
    }

    private static CannonDirectorManager instance;
    private final Object2DoubleOpenHashMap<TNTPrimed> tracking = new Object2DoubleOpenHashMap<>();
    private long lastCheck = 0;

    public static CannonDirectorManager getInstance() {
        return instance;
    }

    public CannonDirectorManager() {
        instance = this;
    }

    @Override
    public void run() {
        long ticksElapsed = (System.currentTimeMillis() - lastCheck) / 50;
        if (ticksElapsed <= 0)
            return;

        processTNTContactExplosives(); //Changed the order so newly directed TNT is not affected by Contact Explosives
        processDirectors();
        // then, removed any exploded or invalid TNT from tracking
        tracking.keySet().removeIf(tnt -> !tnt.isValid() || tnt.getFuseTicks() <= 0);

        lastCheck = System.currentTimeMillis();
    }

    private void processDirectors() {
        // see if there is any new rapid moving TNT in the worlds
        for (World w : Bukkit.getWorlds()) {
            if (w == null)
                continue;

            ArrayList<TNTPrimed> allTNT = new ArrayList<>(w.getEntitiesByClass(TNTPrimed.class));
            for (TNTPrimed tnt : allTNT) {
                if (!(tnt.getVelocity().lengthSquared() > 0.35) || tracking.containsKey(tnt)) {
                    continue;
                }
                tracking.put(tnt, tnt.getVelocity().lengthSquared());

                Craft c = getDirectingCraft(tnt);
                if (c == null) {
                    c = CraftManager.getInstance().fastNearestCraftToLoc(tnt.getLocation());

                    if (c == null || c.getSinking())
                        continue;
                }
                if(!(c instanceof PlayerCraft))
                    continue;

                MovecraftLocation midpoint = c.getHitBox().getMidPoint();
                int distX = Math.abs(midpoint.getX() - tnt.getLocation().getBlockX());
                int distY = Math.abs(midpoint.getY() - tnt.getLocation().getBlockY());
                int distZ = Math.abs(midpoint.getZ() - tnt.getLocation().getBlockZ());
                if (!hasDirector((PlayerCraft) c) || distX >= Config.CannonDirectorDistance || distY >= Config.CannonDirectorDistance || distZ >= Config.CannonDirectorDistance) {
                    continue;
                }
                Player p = getDirector((PlayerCraft) c);
                if (p == null || p.getInventory().getItemInMainHand().getType() != Config.DirectorTool) {
                    continue;
                }
                Vector tntVector = tnt.getVelocity();
                // Store the speed to add it back in later, since all the values we will be using are "normalized", IE: have a speed of 1
                // We're only interested in the horizontal speed for now since that's all directors *should* affect.
                tntVector.setY(0);
                double horizontalSpeed = tntVector.length();
                tntVector = tntVector.normalize(); // you normalize it for comparison with the new direction to see if we are trying to steer too far
                Block targetBlock = DirectorUtils.getDirectorBlock(p);
                Vector targetVector;
                if (targetBlock == null || targetBlock.getType().equals(Material.AIR)) { // the player is looking at nothing, shoot in that general direction
                    targetVector = p.getLocation().getDirection();
                }
                else { // shoot directly at the block the player is looking at (IE: with convergence)
                    targetVector = targetBlock.getLocation().toVector().subtract(tnt.getLocation().toVector());
                }
                // Remove the y-component from the TargetVector and normalize
                targetVector = (new Vector(targetVector.getX(), 0,targetVector.getZ())).normalize();
                // Now set the TNT vector, making sure it falls within the maximum and minimum deflection
                tntVector.setX(Math.min(Math.max(targetVector.getX(), tntVector.getX()-0.7), tntVector.getX()+0.7));
                tntVector.setZ(Math.min(Math.max(targetVector.getZ(), tntVector.getZ()-0.7), tntVector.getZ()+0.7));
                tntVector = tntVector.multiply(horizontalSpeed); // put the original speed back in, but now along a different trajectory
                tntVector.setY(tnt.getVelocity().getY()); // you leave the original Y (or vertical axis) trajectory as it was
                tnt.setVelocity(tntVector);
            }
        }

    }

    private Craft getDirectingCraft(TNTPrimed tnt) {
        if(!Config.EnableTNTTracking)
            return null;

        List<MetadataValue> meta = tnt.getMetadata("MCC-Sender");
        if(meta.isEmpty())
            return null;

        Player sender = Bukkit.getPlayer(UUID.fromString(meta.get(0).asString()));
        if (sender == null || !sender.isOnline())
            return null;

        Craft c = CraftManager.getInstance().getCraftByPlayer(sender);
        if (c == null || c.getSinking())
            return null;
        return c;
    }

    private void processTNTContactExplosives() {
        if(!Config.EnableContactExplosives)
            return;
        // now check to see if any has abruptly changed velocity, and should
        // explode
        for (TNTPrimed tnt : tracking.keySet()) {
            double vel = tnt.getVelocity().lengthSquared();
            if (vel < tracking.getDouble(tnt) / Config.ContactExplosivesMaxImpulseFactor) {
                tnt.setVelocity(new Vector(0,0,0)); //freeze it in place to prevent sliding
                tnt.setFuseTicks(0);
            }
            else {
                // update the tracking with the new velocity so gradual
                // changes do not make TNT explode
                tracking.put(tnt, vel);
            }
        }
    }

    public void removeTNT(TNTPrimed tnt) {
        tracking.removeDouble(tnt);
    }
}
