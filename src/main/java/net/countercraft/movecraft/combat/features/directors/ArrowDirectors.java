package net.countercraft.movecraft.combat.features.directors;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.features.directors.events.CraftDirectEvent;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.utils.DirectorUtils;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.BooleanProperty;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
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

public class ArrowDirectors extends Directors implements Listener {
    public static final NamespacedKey ALLOW_ARROW_DIRECTOR_SIGN = new NamespacedKey("movecraft-combat", "allow_arrow_director_sign");
    private static final String HEADER = "Arrow Director";
    public static int ArrowDirectorDistance = 50;
    public static int ArrowDirectorRange = 120;
    private long lastCheck = 0;

    public ArrowDirectors() {
        super();
    }

    public static void register() {
        CraftType.registerProperty(new BooleanProperty("allowArrowDirectorSign", ALLOW_ARROW_DIRECTOR_SIGN, type -> true));
    }

    public static void load(@NotNull FileConfiguration config) {
        ArrowDirectorDistance = config.getInt("ArrowDirectorDistance", 50);
        ArrowDirectorRange = config.getInt("ArrowDirectorRange", 120);
    }

    @Override
    public void run() {
        long ticksElapsed = (System.currentTimeMillis() - lastCheck) / 50;
        if (ticksElapsed <= 3)
            return;

        for (World w : Bukkit.getWorlds()) {
            if (w == null || w.getPlayers().isEmpty())
                continue;

            var allArrows = w.getEntitiesByClass(Arrow.class);
            for (Arrow arrow : allArrows)
                processArrow(arrow);
        }

        lastCheck = System.currentTimeMillis();
    }

    private void processArrow(@NotNull Arrow arrow) {
        if (arrow.getShooter() instanceof org.bukkit.entity.LivingEntity)
            return;

        Craft c = MathUtils.fastNearestCraftToLoc(CraftManager.getInstance().getCrafts(), arrow.getLocation());
        if (!(c instanceof PlayerCraft) || !hasDirector((PlayerCraft) c))
            return;

        Player p = getDirector((PlayerCraft) c);
        if (p == null || p.getInventory().getItemInMainHand().getType() != Directors.DirectorTool)
            return;

        MovecraftLocation midPoint = c.getHitBox().getMidPoint();
        int distX = Math.abs(midPoint.getX() - arrow.getLocation().getBlockX());
        int distY = Math.abs(midPoint.getY() - arrow.getLocation().getBlockY());
        int distZ = Math.abs(midPoint.getZ() - arrow.getLocation().getBlockZ());
        if (distX > ArrowDirectorDistance || distY > ArrowDirectorDistance || distZ > ArrowDirectorDistance)
            return;

        CraftDirectEvent event = new CraftDirectEvent(c, p, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        arrow.setShooter(p);

        Vector arrowVector = arrow.getVelocity();
        double speed = arrowVector.length(); // store the speed to add it back in later, since all the values we will be using are "normalized", IE: have a speed of 1
        arrowVector = arrowVector.normalize(); // you normalize it for comparison with the new direction to see if we are trying to steer too far

        Block targetBlock = DirectorUtils.getDirectorBlock(p, ArrowDirectorRange);
        Vector targetVector;
        if (targetBlock == null || targetBlock.getType().equals(Material.AIR)) // the player is looking at nothing, shoot in that general direction
            targetVector = p.getLocation().getDirection();
        else { // shoot directly at the block the player is looking at (IE: with convergence)
            targetVector = targetBlock.getLocation().toVector().subtract(arrow.getLocation().toVector());
            targetVector = targetVector.normalize();
        }

        if (targetVector.getX() - arrowVector.getX() > 0.5)
            arrowVector.setX(arrowVector.getX() + 0.5);
        else if (targetVector.getX() - arrowVector.getX() < -0.5)
            arrowVector.setX(arrowVector.getX() - 0.5);
        else
            arrowVector.setX(targetVector.getX());

        if (targetVector.getY() - arrowVector.getY() > 0.5)
            arrowVector.setY(arrowVector.getY() + 0.5);
        else if (targetVector.getY() - arrowVector.getY() < -0.5)
            arrowVector.setY(arrowVector.getY() - 0.5);
        else
            arrowVector.setY(targetVector.getY());

        if (targetVector.getZ() - arrowVector.getZ() > 0.5)
            arrowVector.setZ(arrowVector.getZ() + 0.5);
        else if (targetVector.getZ() - arrowVector.getZ() < -0.5)
            arrowVector.setZ(arrowVector.getZ() - 0.5);
        else
            arrowVector.setZ(targetVector.getZ());

        arrowVector = arrowVector.multiply(speed); // put the original speed back in, but now along a different trajectory
        try {
            arrowVector.checkFinite();
        }
        catch (IllegalArgumentException ignored) {
            return;
        }
        arrow.setVelocity(arrowVector);
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

        if (!foundCraft.getType().getBoolProperty(ALLOW_ARROW_DIRECTOR_SIGN)) {
            p.sendMessage(ERROR_PREFIX + " " + I18nSupport.getInternationalisedString("ArrowDirector - Not Allowed On Craft"));
            return;
        }

        if (action == Action.LEFT_CLICK_BLOCK) {
            if (!isDirector(p))
                return;

            removeDirector(p);
            p.sendMessage(I18nSupport.getInternationalisedString("ArrowDirector - No Longer Directing"));
            return;
        }

        clearDirector(p);
        addDirector(foundCraft, p);
        p.sendMessage(I18nSupport.getInternationalisedString("ArrowDirector - Directing"));
    }
}
