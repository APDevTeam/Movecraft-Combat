package net.countercraft.movecraft.combat.movecraftcombat.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        if(craft.getSinking() && e.getReason() == CraftReleaseEvent.Reason.DISCONNECT) {
            e.setCancelled(true);
            return;
        }
        if(craft.getNotificationPlayer() == null)
            return;

        Player player = craft.getNotificationPlayer();
        if(!isInCombat(player))
            return;
        records.remove(player);

        CraftReleaseEvent.Reason reason = e.getReason();
        if(craft.getType().getMustBeSubcraft())
            return;
        if(reason != CraftReleaseEvent.Reason.PLAYER)
            return;
        if(craft.getType().getCruiseOnPilot())
            return;

        stopCombat(player);

        if(craft.getSinking() || isInAirspace(craft))
            return;

        MovecraftCombat.getInstance().getLogger().severe("Combat release! reason:" + reason);
        CombatReleaseEvent event = new CombatReleaseEvent(craft, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled())
            return;

        if(Config.CombatReleaseScuttle) {
            e.setCancelled(true);
            craft.sink();
        }
        if(Config.CombatReleaseBanLength > 0) {
            Date expiry = new Date(System.currentTimeMillis() + Config.CombatReleaseBanLength * 1000);
            Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), "Combat release!", expiry, "Movecraft-Combat AutoBan");
        }
        if(Config.CombatReleaseBanLength > 0 || Config.EnableCombatReleaseKick)
            player.kickPlayer("Combat release!");
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
        player.sendMessage(ChatColor.RED + "You have now entered combat.");
        Bukkit.getLogger().info("[Movecraft-Combat] " + player.getName() + " has entered combat.");
    }

    private void stopCombat(@NotNull Player player) {
        if(!isInCombat(player))
            return;
        Bukkit.getServer().getPluginManager().callEvent(new CombatStopEvent(player));
        player.sendMessage(ChatColor.RED + "You have now left combat.");
        Bukkit.getLogger().info("[Movecraft-Combat] " + player.getName() + " has left combat.");
    }

    // TODO: Replace this functionality with a listener in MCWG
    private boolean isInAirspace(@NotNull Craft craft) {
        if(MovecraftCombat.getInstance().getWGPlugin() == null)
            return false;

        for(MovecraftLocation l : getHitboxCorners(craft.getHitBox())) {
            ApplicableRegionSet regions = MovecraftCombat.getInstance().getWGPlugin().getRegionManager(craft.getW()).getApplicableRegions(l.toBukkit(craft.getW()));
            for(ProtectedRegion r : regions.getRegions()) {
                if(r.getFlag(DefaultFlag.TNT) == StateFlag.State.DENY || r.getFlag(DefaultFlag.PVP) == StateFlag.State.DENY)
                    return true;
            }
        }
        return false;
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
