package net.countercraft.movecraft.combat.movecraftcombat.status;

import java.util.*;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.movecraftcombat.utils.WorldGuard6Utils;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import net.countercraft.movecraft.combat.movecraftcombat.event.CombatReleaseEvent;
import net.countercraft.movecraft.combat.movecraftcombat.event.CombatStartEvent;
import net.countercraft.movecraft.combat.movecraftcombat.event.CombatStopEvent;


public class StatusManager extends BukkitRunnable {
    private static StatusManager instance;
    private final HashMap<Player, Long> records = new HashMap<>();

    public StatusManager() {
        instance = this;
    }

    public static StatusManager getInstance() {
        return instance;
    }


    public void run() {
        long currentTime = System.currentTimeMillis();
        HashSet<Player> removeSet = new HashSet<>();
        for(Player player : records.keySet()) {
            if((currentTime - records.get(player)) > Config.DamageTimeout * 1000) {
                removeSet.add(player);
            }
        }
        for(Player player : removeSet) {
            stopCombat(player);
            records.remove(player);
        }
    }


    public boolean isInCombat(Player player) {
        if(!Config.EnableCombatReleaseTracking)
            return false;
        if(!records.containsKey(player))
            return false;
        return System.currentTimeMillis() - records.get(player) < Config.DamageTimeout * 1000;
    }

    public void registerEvent(@Nullable Player player) {
        if(!Config.EnableCombatReleaseTracking)
            return;
        if(player == null)
            return;
        if(!records.containsKey(player) || System.currentTimeMillis() - records.get(player) > Config.DamageTimeout * 1000)
            startCombat(player);
        records.put(player, System.currentTimeMillis());
    }

    public void craftReleased(@NotNull CraftReleaseEvent e) {
        if(!Config.EnableCombatReleaseTracking)
            return;
        Craft craft = e.getCraft();
        if(craft.getSinking() || craft.getNotificationPlayer() == null)
            return;

        CraftReleaseEvent.Reason reason = e.getReason();
        if(craft.getType().getMustBeSubcraft())
            return;
        if(reason != CraftReleaseEvent.Reason.PLAYER && reason != CraftReleaseEvent.Reason.DISCONNECT)
            return;
        if(craft.getType().getCruiseOnPilot())
            return;

        Player player = craft.getNotificationPlayer();
        if(!isInCombat(player))
            return;
        records.remove(player);

        stopCombat(player);

        if(isInAirspace(craft))
            return;

        if(player.hasPermission("movecraft.combat.bypass")) {
            return;
        }

        MovecraftCombat.getInstance().getLogger().info(I18nSupport.getInternationalisedString("Combat Release") + " " + player.getName());
        CombatReleaseEvent event = new CombatReleaseEvent(craft, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled())
            return;

        if(Config.CombatReleaseScuttle) {
            player.sendMessage(ChatColor.RED + I18nSupport.getInternationalisedString("Combat Release Message"));
            e.setCancelled(true);
            craft.setNotificationPlayer(null);
            craft.sink();
        }
        if(Config.CombatReleaseBanLength > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Date expiry = new Date(System.currentTimeMillis() + Config.CombatReleaseBanLength * 1000);
                    Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), I18nSupport.getInternationalisedString("Combat Release"), expiry, "Movecraft-Combat AutoBan");
                }
            }.runTaskLater(MovecraftCombat.getInstance(), 5);
        }
        if(Config.CombatReleaseBanLength > 0 || Config.EnableCombatReleaseKick) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(player == null || !player.isOnline()) {
                        return;
                    }
                    player.kickPlayer(I18nSupport.getInternationalisedString("Combat Release"));
                }
            }.runTaskLater(MovecraftCombat.getInstance(), 5);
        }
    }

    public void craftSunk(@NotNull Craft craft) {
        if(!Config.EnableCombatReleaseTracking)
            return;
        if(craft.getNotificationPlayer() == null)
            return;
        if(craft.getType().getCruiseOnPilot())
            return;

        Player player = craft.getNotificationPlayer();
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

    private boolean isInAirspace(@NotNull Craft craft) {
        if(MovecraftCombat.getInstance().getWGPlugin() == null)
            return false;

        return WorldGuard6Utils.isInAirspace(craft);
    }

    // TODO: Move this to HitBox or somewhere in Utils of MC
    private ArrayList<MovecraftLocation> getHitboxCorners(@NotNull HitBox hitbox) {
        ArrayList<MovecraftLocation> corners = new ArrayList<>();
        corners.add(new MovecraftLocation(hitbox.getMinX(), hitbox.getMinY(), hitbox.getMinZ()));
        corners.add(new MovecraftLocation(hitbox.getMinX(), hitbox.getMinY(), hitbox.getMaxZ()));
        corners.add(new MovecraftLocation(hitbox.getMinX(), hitbox.getMaxY(), hitbox.getMinZ()));
        corners.add(new MovecraftLocation(hitbox.getMinX(), hitbox.getMaxY(), hitbox.getMaxZ()));
        corners.add(new MovecraftLocation(hitbox.getMaxX(), hitbox.getMinY(), hitbox.getMinZ()));
        corners.add(new MovecraftLocation(hitbox.getMaxX(), hitbox.getMinY(), hitbox.getMaxZ()));
        corners.add(new MovecraftLocation(hitbox.getMaxX(), hitbox.getMaxY(), hitbox.getMinZ()));
        corners.add(new MovecraftLocation(hitbox.getMaxX(), hitbox.getMaxY(), hitbox.getMaxZ()));
        return corners;
    }
}
