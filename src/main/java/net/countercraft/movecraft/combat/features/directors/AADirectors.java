package net.countercraft.movecraft.combat.features.directors;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.features.directors.events.CraftDirectEvent;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.utils.DirectorUtils;
import net.countercraft.movecraft.combat.utils.MathHelper;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.BooleanProperty;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;

public class AADirectors extends Directors implements Listener {
    public static final NamespacedKey ALLOW_AA_DIRECTOR_SIGN = new NamespacedKey("movecraft-combat", "allow_aa_director_sign");
    private static final String HEADER = "AA Director";
    public static int AADirectorDistance = 50;
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

        Player p = getDirector((PlayerCraft) c);
        if (p == null || p.getInventory().getItemInMainHand().getType() != Directors.DirectorTool)
            return;

        MovecraftLocation midPoint = c.getHitBox().getMidPoint();
        int distX = Math.abs(midPoint.getX() - fireball.getLocation().getBlockX());
        int distY = Math.abs(midPoint.getY() - fireball.getLocation().getBlockY());
        int distZ = Math.abs(midPoint.getZ() - fireball.getLocation().getBlockZ());
        if (distX > AADirectorDistance || distY > AADirectorDistance || distZ > AADirectorDistance)
            return;

        CraftDirectEvent event = new CraftDirectEvent(c, p, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        fireball.setShooter(p);

        Vector fireballVector = fireball.getVelocity();
        double speed = fireballVector.length() ; // store the speed to add it back in later, since all the values we will be using are "normalized", IE: have a speed of 1
        fireballVector = fireballVector.normalize(); // you normalize it for comparison with the new direction to see if we are trying to steer too far

        // shoot in that general direction
        Vector targetVector = p.getLocation().getDirection();
        if (AADirectorRange >= 0) {
            // Range is greater than zero and the player is looking at a block, direct at it (IE: with convergence)
            Block targetBlock = DirectorUtils.getDirectorBlock(p, AADirectorRange);
            if (targetBlock != null && !targetBlock.getType().isAir()) {
                targetVector = targetBlock.getLocation().toVector().subtract(fireball.getLocation().toVector());
                targetVector = targetVector.normalize();
            }
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

        MathHelper.clampVectorModify(fireballVector);

        try {
            fireballVector.checkFinite();
        }
        catch (IllegalArgumentException ignored) {
            return;
        }
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

        e.setCancelled(true);
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
            return;
        }

        clearDirector(p);
        addDirector(foundCraft, p);
        p.sendMessage(I18nSupport.getInternationalisedString("AADirector - Directing"));
    }
}
