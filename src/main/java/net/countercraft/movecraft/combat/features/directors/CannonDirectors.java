package net.countercraft.movecraft.combat.features.directors;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.features.directors.events.CraftDirectEvent;
import net.countercraft.movecraft.combat.features.tracking.DamageTracking;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.utils.DirectorUtils;
import net.countercraft.movecraft.combat.utils.MathHelper;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.property.BooleanProperty;
import net.countercraft.movecraft.util.ChatUtils;
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

import java.util.List;
import java.util.UUID;

import static net.countercraft.movecraft.util.ChatUtils.errorPrefix;

public class CannonDirectors extends Directors implements Listener {
    public static final NamespacedKey ALLOW_CANNON_DIRECTOR_SIGN = new NamespacedKey("movecraft-combat", "allow_cannon_director_sign");
    private static final String HEADER = "Cannon Director";
    private static boolean DisableDirectorElytra = false;
    public static int CannonDirectorDistance = 100;
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
        CannonDirectorRange = config.getInt("CannonDirectorRange", 120);
        DisableDirectorElytra = config.getBoolean("DisableDirectorElytra", false);
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

        MovecraftLocation midpoint = c.getHitBox().getMidPoint();
        int distX = Math.abs(midpoint.getX() - tnt.getLocation().getBlockX());
        int distY = Math.abs(midpoint.getY() - tnt.getLocation().getBlockY());
        int distZ = Math.abs(midpoint.getZ() - tnt.getLocation().getBlockZ());
        if (!hasDirector((PlayerCraft) c) || distX >= CannonDirectorDistance
                || distY >= CannonDirectorDistance || distZ >= CannonDirectorDistance)
            return;

        Player p = getDirector((PlayerCraft) c);
        if (p == null || p.getInventory().getItemInMainHand().getType() != Directors.DirectorTool)
            return;

        CraftDirectEvent event = new CraftDirectEvent(c, p, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        // Store the speed to add it back in later, since all the values we will be using are "normalized", IE: have a speed of 1
        // We're only interested in the horizontal speed for now since that's all directors *should* affect.
        Vector tntVector = tnt.getVelocity();
        tntVector.setY(0);
        double horizontalSpeed = tntVector.length();
        tntVector = tntVector.normalize(); // you normalize it for comparison with the new direction to see if we are trying to steer too far

        // the player is looking at nothing, shoot in that general direction
        Vector targetVector = p.getLocation().getDirection();

        if (CannonDirectorRange >= 0) {
            Block targetBlock = DirectorUtils.getDirectorBlock(p, CannonDirectorRange);
            if (targetBlock != null && targetBlock.getType().equals(Material.AIR)) {
                // shoot directly at the block the player is looking at (IE: with convergence)
                targetVector = targetBlock.getLocation().toVector().subtract(tnt.getLocation().toVector());
            }
        }

        // Remove the y-component from the TargetVector and normalize
        targetVector = (new Vector(targetVector.getX(), 0, targetVector.getZ())).normalize();

        // Now set the TNT vector, making sure it falls within the maximum and minimum deflection
        tntVector.setX(Math.min(Math.max(targetVector.getX(), tntVector.getX() - 0.7), tntVector.getX() + 0.7));
        tntVector.setZ(Math.min(Math.max(targetVector.getZ(), tntVector.getZ() - 0.7), tntVector.getZ() + 0.7));

        tntVector = tntVector.multiply(horizontalSpeed); // put the original speed back in, but now along a different trajectory

        MathHelper.clampVectorModify(tntVector);

        tntVector.setY(tnt.getVelocity().getY()); // you leave the original Y (or vertical axis) trajectory as it was

        try {
            tntVector.checkFinite();
        }
        catch (IllegalArgumentException ignored) {
            return;
        }
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
            p.sendMessage(errorPrefix() + " " + I18nSupport.getInternationalisedString("Sign - Must Be Part Of Craft"));
            return;
        }

        if (!foundCraft.getType().getBoolProperty(ALLOW_CANNON_DIRECTOR_SIGN)) {
            p.sendMessage(errorPrefix() + " " + I18nSupport.getInternationalisedString("CannonDirector - Not Allowed On Craft"));
            return;
        }

        if (action == Action.LEFT_CLICK_BLOCK) {
            if (!isDirector(p))
                return;

            removeDirector(p);
            p.sendMessage(I18nSupport.getInternationalisedString("CannonDirector - No Longer Directing"));
            return;
        }

        // check if the player has an elytra on
        if (DisableDirectorElytra) {
            if (p.getInventory().getChestplate() != null) {
                if (p.getInventory().getChestplate().getType().equals(Material.ELYTRA)) {
                    p.sendMessage(I18nSupport.getInternationalisedString("CannonDirector - No Elytra While Directing"));
                    clearDirector(p);
                    return;
                }
            }
        }

        clearDirector(p);
        addDirector(foundCraft, p);
        p.sendMessage(I18nSupport.getInternationalisedString("CannonDirector - Directing"));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof TNTPrimed))
            return;

        tracking.removeDouble(e.getEntity());
    }
}
