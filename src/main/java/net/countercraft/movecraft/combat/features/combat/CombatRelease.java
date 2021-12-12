package net.countercraft.movecraft.combat.features.combat;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.event.CollisionDamagePlayerCraftEvent;
import net.countercraft.movecraft.combat.features.combat.events.CombatReleaseEvent;
import net.countercraft.movecraft.combat.features.combat.events.CombatStartEvent;
import net.countercraft.movecraft.combat.features.combat.events.CombatStopEvent;
import net.countercraft.movecraft.combat.features.tracking.DamageTracking;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftScuttleEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;

public class CombatRelease extends BukkitRunnable implements Listener {
    public static boolean EnableCombatReleaseTracking = false;
    public static boolean EnableCombatReleaseKick = true;
    public static long CombatReleaseBanLength = 60;
    public static boolean CombatReleaseScuttle = true;

    public static void load(@NotNull FileConfiguration config) {
        EnableCombatReleaseTracking = config.getBoolean("EnableCombatReleaseTracking", false);
        EnableCombatReleaseKick = config.getBoolean("EnableCombatReleaseKick", true);
        CombatReleaseBanLength = config.getLong("CombatReleaseBanLength", 60);
        CombatReleaseScuttle = config.getBoolean("CombatReleaseScuttle", true);
    }

    @Nullable
    private static CombatRelease instance;

    @Nullable
    public static CombatRelease getInstance() {
        return instance;
    }

    private final HashMap<Player, Long> records = new HashMap<>();

    public CombatRelease() {
        instance = this;
    }

    public void run() {
        long currentTime = System.currentTimeMillis();
        HashSet<Player> removeSet = new HashSet<>();
        for(var entry : records.entrySet()) {
            if((currentTime - entry.getValue()) > DamageTracking.DamageTimeout * 1000L)
                removeSet.add(entry.getKey());
        }
        for(Player player : removeSet) {
            stopCombat(player);
            records.remove(player);
        }
    }


    public boolean isInCombat(Player player) {
        if(!EnableCombatReleaseTracking)
            return false;
        if(!records.containsKey(player))
            return false;

        return System.currentTimeMillis() - records.get(player) < DamageTracking.DamageTimeout * 1000L;
    }

    public void registerEvent(@Nullable Player player) {
        if(!EnableCombatReleaseTracking)
            return;
        if(player == null)
            return;
        if(!records.containsKey(player) || System.currentTimeMillis() - records.get(player) > DamageTracking.DamageTimeout * 1000L)
            startCombat(player);
        records.put(player, System.currentTimeMillis());
    }

    public void craftSunk(@NotNull PlayerCraft craft) {
        if(!EnableCombatReleaseTracking)
            return;

        Player player = craft.getPilot();
        records.remove(player);
        stopCombat(player);
    }

    private void startCombat(@NotNull Player player) {
        if(isInCombat(player))
            return;

        Bukkit.getServer().getPluginManager().callEvent(new CombatStartEvent(player));
        player.sendMessage(ChatColor.RED + I18nSupport.getInternationalisedString("Status - Enter Combat"));
        MovecraftCombat.getInstance().getLogger().info(player.getName() + " " + I18nSupport.getInternationalisedString("Log - Enter Combat"));
    }

    private void stopCombat(@NotNull Player player) {
        Bukkit.getServer().getPluginManager().callEvent(new CombatStopEvent(player));
        player.sendMessage(ChatColor.RED + I18nSupport.getInternationalisedString("Status - Leave Combat"));
        MovecraftCombat.getInstance().getLogger().info(player.getName() + " " + I18nSupport.getInternationalisedString("Log - Leave Combat"));
    }

    private boolean canManOverboard(Player player, @NotNull Craft craft) {
        if(craft.getDisabled())
            return false;
        if (craft.getWorld() != player.getWorld())
            return false;

        Location telPoint = MovecraftLocation.toBukkit(craft.getWorld(), craft.getHitBox().getMidPoint());
        return telPoint.distanceSquared(player.getLocation()) < Settings.ManOverboardDistSquared;
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCollisionDamagePlayerCraft(@NotNull CollisionDamagePlayerCraftEvent e) {
        registerEvent(e.getDamaged().getPilot());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftRelease(@NotNull CraftReleaseEvent e) {
        if(!EnableCombatReleaseTracking)
            return;

        Craft craft = e.getCraft();
        if(craft.getSinking())
            return;
        if(!(craft instanceof PlayerCraft))
            return;
        CraftReleaseEvent.Reason reason = e.getReason();
        if(reason != CraftReleaseEvent.Reason.PLAYER && reason != CraftReleaseEvent.Reason.DISCONNECT)
            return;
        if(craft.getType().getBoolProperty(CraftType.CRUISE_ON_PILOT))
            return;

        Player player = ((PlayerCraft) craft).getPilot();
        if(!isInCombat(player))
            return;
        records.remove(player);

        stopCombat(player);

        if(player.hasPermission("movecraft.combat.bypass"))
            return;

        MovecraftCombat.getInstance().getLogger().info(I18nSupport.getInternationalisedString("Combat Release") + " " + player.getName());
        CombatReleaseEvent event = new CombatReleaseEvent(craft, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled())
            return;

        player.sendMessage(ChatColor.RED + I18nSupport.getInternationalisedString("Combat Release Message"));

        if(CombatReleaseScuttle) {
            e.setCancelled(true);
            craft.setNotificationPlayer(null);
            craft.setCruising(false);
            craft.sink();
        }

        if(!EnableCombatReleaseKick)
            return;
        if(!canManOverboard(player, craft))
            return;

        if(CombatReleaseBanLength > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Date expiry = new Date(System.currentTimeMillis() + CombatReleaseBanLength * 1000);
                    Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), I18nSupport.getInternationalisedString("Combat Release"), expiry, "Movecraft-Combat AutoBan");
                }
            }.runTaskLater(MovecraftCombat.getInstance(), 5);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!player.isOnline())
                    return;

                player.kickPlayer(I18nSupport.getInternationalisedString("Combat Release"));
            }
        }.runTaskLater(MovecraftCombat.getInstance(), 5);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraftScuttle(@NotNull CraftScuttleEvent e) {
        if(!EnableCombatReleaseTracking)
            return;

        Player cause = e.getCause();
        if(e.getCraft() instanceof PilotedCraft) {
            Player pilot = ((PilotedCraft) e.getCraft()).getPilot();
            if(pilot != cause)
                return; // Always let /scuttle [player] run.
        }

        if(!isInCombat(cause))
            return;

        e.setCancelled(true);
        cause.sendMessage(ERROR_PREFIX + " You may not scuttle while in combat!");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftSink(@NotNull CraftSinkEvent e) {
        if(!(e.getCraft() instanceof PlayerCraft))
            return;

        craftSunk((PlayerCraft) e.getCraft());
    }
}
