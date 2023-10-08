package net.countercraft.movecraft.combat.features.directors;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.utils.DirectorUtils;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.BooleanProperty;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;

public class AADirectors extends Directors implements Listener {
    public static final NamespacedKey ALLOW_AA_DIRECTOR_SIGN = new NamespacedKey("movecraft-combat", "allow_aa_director_sign");
    private static final String HEADER = "AA Director";
    public static int AADirectorDistance = 50;
    public static int AADirectorNodeDistance = 3;
    public static int AADirectorRange = 120;
    private long lastCheck = 0;

    public AADirectors() {
        super();
    }

    public static void register() {
        CraftType.registerProperty(new BooleanProperty("allowAADirectorSign", ALLOW_AA_DIRECTOR_SIGN, type -> true));
    }

    public static void load(@NotNull FileConfiguration config) {
        AADirectorDistance = config.getInt("AADirectorDistance", 50);
        AADirectorNodeDistance = config.getInt("AADirectorNodeDistance", 3);
        AADirectorRange = config.getInt("AADirectorRange", 120);
    }

    @Override
    public void run() {
        long ticksElapsed = (System.currentTimeMillis() - lastCheck) / 50;
        if (ticksElapsed <= 3)
            return;

        for (World w : Bukkit.getWorlds()) {
            if (w == null || w.getPlayers().size() == 0)
                continue;

            var allFireballs = w.getEntitiesByClass(SmallFireball.class);
            for (SmallFireball fireball : allFireballs)
                processFireball(fireball);
        }

        lastCheck = System.currentTimeMillis();
    }

    private void processFireball(@NotNull SmallFireball fireball) {
        if (fireball.getShooter() instanceof org.bukkit.entity.LivingEntity)
            return;

        Craft c = MathUtils.fastNearestCraftToLoc(CraftManager.getInstance().getCrafts(), fireball.getLocation());
        if (!(c instanceof PlayerCraft) || !hasDirector((PlayerCraft) c))
            return;

        HashSet<DirectorData> craftDirectors = getCraftDirectors((PlayerCraft) c);
        Player player;
        Player dominantPlayer = null;

        for (DirectorData data : craftDirectors) {
            if (data.getSelectedNodes().isEmpty() || data.getSignLocations().isEmpty()) {
                dominantPlayer = data.getPlayer();
            }
        }
        if (dominantPlayer != null) {
            player = dominantPlayer;
        } else {
            player = getClosestDirectorFromProjectile(
                    craftDirectors,
                    fireball.getLocation().toVector(),
                    AADirectorNodeDistance
            );
        }

        if (player == null || player.getInventory().getItemInMainHand().getType() != Directors.DirectorTool)
            return;

        MovecraftLocation midpoint = c.getHitBox().getMidPoint();
        int distX = Math.abs(midpoint.getX() - fireball.getLocation().getBlockX());
        int distY = Math.abs(midpoint.getY() - fireball.getLocation().getBlockY());
        int distZ = Math.abs(midpoint.getZ() - fireball.getLocation().getBlockZ());
        if (distX*distX + distY*distY + distZ*distZ >= AADirectorDistance*AADirectorDistance)
            return;

        fireball.setShooter(player);

        Vector fireballVector = fireball.getVelocity();
        double speed = fireballVector.length(); // store the speed to add it back in later, since all the values we will be using are "normalized", IE: have a speed of 1
        fireballVector = fireballVector.normalize(); // you normalize it for comparison with the new direction to see if we are trying to steer too far

        Block targetBlock = DirectorUtils.getDirectorBlock(player, AADirectorRange);
        Vector targetVector;
        if (targetBlock == null || targetBlock.getType().equals(Material.AIR)) // the player is looking at nothing, shoot in that general direction
            targetVector = player.getLocation().getDirection();
        else { // shoot directly at the block the player is looking at (IE: with convergence)
            targetVector = targetBlock.getLocation().toVector().subtract(fireball.getLocation().toVector());
            targetVector = targetVector.normalize();
        }

        if (targetVector.getX() - fireballVector.getX() > 0.5)
            fireballVector.setX(fireballVector.getX() + 0.5);
        else if (targetVector.getX() - fireballVector.getX() < -0.5)
            fireballVector.setX(fireballVector.getX() - 0.5);
        else
            fireballVector.setX(targetVector.getX());

        if (targetVector.getY() - fireballVector.getY() > 0.5)
            fireballVector.setY(fireballVector.getY() + 0.5);
        else if (targetVector.getY() - fireballVector.getY() < -0.5)
            fireballVector.setY(fireballVector.getY() - 0.5);
        else
            fireballVector.setY(targetVector.getY());

        if (targetVector.getZ() - fireballVector.getZ() > 0.5)
            fireballVector.setZ(fireballVector.getZ() + 0.5);
        else if (targetVector.getZ() - fireballVector.getZ() < -0.5)
            fireballVector.setZ(fireballVector.getZ() - 0.5);
        else
            fireballVector.setZ(targetVector.getZ());

        fireballVector = fireballVector.multiply(speed); // put the original speed back in, but now along a different trajectory
        fireball.setVelocity(fireballVector);
        fireball.setDirection(fireballVector);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignClick(@NotNull PlayerInteractEvent e) {
        var action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK)
            return;

        Block b = e.getClickedBlock();
        if (b == null)
            throw new IllegalStateException();
        var state = b.getState();
        if (!(state instanceof Sign))
            return;

        Sign sign = (Sign) state;
        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(HEADER))
            return;

        PlayerCraft foundCraft = null;
        for (Craft c : CraftManager.getInstance()) {
            if (!(c instanceof PlayerCraft))
                continue;
            if (!c.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation())))
                continue;
            foundCraft = (PlayerCraft) c;
            break;
        }

        Player p = e.getPlayer();
        if (foundCraft == null) {
            p.sendMessage(ERROR_PREFIX + " " + I18nSupport.getInternationalisedString("Sign - Must Be Part Of Craft"));
            return;
        }

        if (!foundCraft.getType().getBoolProperty(ALLOW_AA_DIRECTOR_SIGN)) {
            p.sendMessage(ERROR_PREFIX + " " + I18nSupport.getInternationalisedString("AADirector - Not Allowed On Craft"));
            return;
        }

        if (action == Action.LEFT_CLICK_BLOCK) {
            if (!isDirector(p))
                return;

            removeDirector(p);
            p.sendMessage(I18nSupport.getInternationalisedString("AADirector - No Longer Directing"));
            e.setCancelled(true);
            return;
        }

        Set<String> selectedLines = processSign(sign);
        if (isNodesShared(selectedLines, foundCraft, p)) {
            p.sendMessage(ERROR_PREFIX + " " + I18nSupport.getInternationalisedString("AADirector - Must Not Share Nodes"));
            return;
        }

        clearDirector(p);
        addDirector(p, foundCraft, selectedLines);

        p.sendMessage(I18nSupport.getInternationalisedString("AADirector - Directing"));
    }
}
