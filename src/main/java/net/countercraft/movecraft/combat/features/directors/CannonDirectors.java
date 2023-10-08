package net.countercraft.movecraft.combat.features.directors;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.features.tracking.DamageTracking;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.utils.DirectorUtils;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.BooleanProperty;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.TNT;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;

public class CannonDirectors extends Directors implements Listener {
    public static final NamespacedKey ALLOW_CANNON_DIRECTOR_SIGN = new NamespacedKey("movecraft-combat", "allow_cannon_director_sign");
    private static final String HEADER = "Cannon Director";
    public static int CannonDirectorDistance = 100;
    public static int CannonDirectorNodeDistance = 3;
    public static int CannonDirectorRange = 120;
    private final Object2DoubleOpenHashMap<TNTPrimed> tracking = new Object2DoubleOpenHashMap<>();
    private long lastCheck = 0;

    public CannonDirectors() {
        super();
    }

    public static void register() {
        CraftType.registerProperty(new BooleanProperty("allowCannonDirectorSign", ALLOW_CANNON_DIRECTOR_SIGN, type -> true));
    }

    public static void load(@NotNull FileConfiguration config) {
        CannonDirectorDistance = config.getInt("CannonDirectorDistance", 100);
        CannonDirectorNodeDistance = config.getInt("CannonDirectorNodeDistance", 3);
        CannonDirectorRange = config.getInt("CannonDirectorRange", 120);
    }

    @Override
    public void run() {
        long ticksElapsed = (System.currentTimeMillis() - lastCheck) / 50;
        if (ticksElapsed <= 0)
            return;

        // see if there is any new rapid moving TNT in the worlds
        for (World w : Bukkit.getWorlds()) {
            if (w == null || w.getPlayers().size() == 0)
                continue;

            var allTNT = w.getEntitiesByClass(TNTPrimed.class);
            for (TNTPrimed tnt : allTNT)
                processTNT(tnt);
        }

        // then, removed any exploded or invalid TNT from tracking
        tracking.keySet().removeIf(tnt -> !tnt.isValid() || tnt.getFuseTicks() <= 0);

        lastCheck = System.currentTimeMillis();
    }

    private void processTNT(@NotNull TNTPrimed tnt) {
        if (!(tnt.getVelocity().lengthSquared() > 0.35) || tracking.containsKey(tnt))
            return;

        tracking.put(tnt, tnt.getVelocity().lengthSquared());

        Craft c = getDirectingCraft(tnt);
        if (c == null) {
            c = MathUtils.fastNearestCraftToLoc(CraftManager.getInstance().getCrafts(), tnt.getLocation());

            if (c == null || c instanceof SinkingCraft)
                return;
        }
        if (!(c instanceof PlayerCraft))
            return;

        // Automatically calibrate the TNT location based on its velocity to make it closer to the firing point.
        Location correctedLocation = tnt.getLocation().clone().add(tnt.getVelocity().clone().multiply(-1.2));
        Vector correctedPosition = correctedLocation.toVector();

        HashSet<DirectorData> craftDirectors = getCraftDirectors((PlayerCraft) c);
        Player player = null;
        for (DirectorData data : craftDirectors) {
            if (data.getSelectedNodes().isEmpty()) {
                player = data.getPlayer();
            }
        }
        if (player == null) {
            player = getClosestDirectorFromProjectile(
                    craftDirectors,
                    correctedPosition,
                    CannonDirectorNodeDistance
            );
        }

        if (player == null || player.getInventory().getItemInMainHand().getType() != Directors.DirectorTool)
            return;

        MovecraftLocation midpoint = c.getHitBox().getMidPoint();
        int distX = Math.abs(midpoint.getX() - correctedPosition.getBlockX());
        int distY = Math.abs(midpoint.getY() - correctedPosition.getBlockY());
        int distZ = Math.abs(midpoint.getZ() - correctedPosition.getBlockZ());
        if (distX*distX + distY*distY + distZ*distZ >= CannonDirectorDistance*CannonDirectorDistance)
            return;

        // Store the speed to add it back in later, since all the values we will be using are "normalized", IE: have a speed of 1
        // We're only interested in the horizontal speed for now since that's all directors *should* affect.
        Vector tntVector = tnt.getVelocity();
        tntVector.setY(0);
        double horizontalSpeed = tntVector.length();
        tntVector = tntVector.normalize(); // you normalize it for comparison with the new direction to see if we are trying to steer too far

        Block targetBlock = DirectorUtils.getDirectorBlock(player, CannonDirectorRange);
        Vector targetVector;
        if (targetBlock == null || targetBlock.getType().equals(Material.AIR)) // the player is looking at nothing, shoot in that general direction
            targetVector = player.getLocation().getDirection();
        else // shoot directly at the block the player is looking at (IE: with convergence)
            targetVector = targetBlock.getLocation().toVector().subtract(tnt.getLocation().toVector());

        // Remove the y-component from the TargetVector and normalize
        targetVector = (new Vector(targetVector.getX(), 0, targetVector.getZ())).normalize();

        // Now set the TNT vector, making sure it falls within the maximum and minimum deflection
        tntVector.setX(Math.min(Math.max(targetVector.getX(), tntVector.getX() - 0.7), tntVector.getX() + 0.7));
        tntVector.setZ(Math.min(Math.max(targetVector.getZ(), tntVector.getZ() - 0.7), tntVector.getZ() + 0.7));

        tntVector = tntVector.multiply(horizontalSpeed); // put the original speed back in, but now along a different trajectory
        tntVector.setY(tnt.getVelocity().getY()); // you leave the original Y (or vertical axis) trajectory as it was

        tnt.setVelocity(tntVector);
    }

    @Nullable
    private Craft getDirectingCraft(@NotNull TNTPrimed tnt) {
        if (!DamageTracking.EnableTNTTracking)
            return null;

        List<MetadataValue> meta = tnt.getMetadata("MCC-Sender");
        if (meta.isEmpty())
            return null;

        Player sender = Bukkit.getPlayer(UUID.fromString(meta.get(0).asString()));
        if (sender == null || !sender.isOnline())
            return null;

        Craft c = CraftManager.getInstance().getCraftByPlayer(sender);
        if (c == null || c instanceof SinkingCraft)
            return null;

        return c;
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public final void onSignClick(@NotNull PlayerInteractEvent e) {
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

        if (!foundCraft.getType().getBoolProperty(ALLOW_CANNON_DIRECTOR_SIGN)) {
            p.sendMessage(ERROR_PREFIX + " " + I18nSupport.getInternationalisedString("CannonDirector - Not Allowed On Craft"));
            return;
        }

        if (action == Action.LEFT_CLICK_BLOCK) {
            if (!isDirector(p))
                return;

            removeDirector(p);
            p.sendMessage(I18nSupport.getInternationalisedString("CannonDirector - No Longer Directing"));
            e.setCancelled(true);
            return;
        }

        Set<String> selectedLines = processSign(sign);
        if (isNodesShared(selectedLines, foundCraft, p)) {
            p.sendMessage(ERROR_PREFIX + " " + I18nSupport.getInternationalisedString("CannonDirector - Must Not Share Nodes"));
            return;
        }

        clearDirector(p);
        addDirector(p, foundCraft, selectedLines);

        p.sendMessage(I18nSupport.getInternationalisedString("CannonDirector - Directing"));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof TNTPrimed))
            return;

        tracking.removeDouble(e.getEntity());
    }
}
