package net.countercraft.movecraft.combat.movecraftcombat.status;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.countercraft.movecraft.craft.Craft;
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
        if(!records.containsKey(player))
            return false;
        return System.currentTimeMillis() - records.get(player) < Config.DamageTimeout * 1000;
    }

    public void registerEvent(@Nullable Player player) {
        if(player == null)
            return;
        if(!records.containsKey(player) || records.get(player) > Config.DamageTimeout * 1000)
            startCombat(player);
        records.put(player, System.currentTimeMillis());
    }

    public void craftReleased(@NotNull Craft craft) {
        if(!Config.EnableCombatReleaseTracking)
            return;
        if(craft.getNotificationPlayer() == null)
            return;

        Player player = craft.getNotificationPlayer();
        if(craft.getSinking()) {
            records.remove(player);
            stopCombat(player);
        }

        if(!isInCombat(player) || isInAirspace(craft))
            return;
        records.remove(player);

        CombatReleaseEvent event = new CombatReleaseEvent(craft, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled())
            return;

        Date expiry = new Date(System.currentTimeMillis() + Config.CombatReleaseBanLength * 1000);
        Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), "Combat release!", expiry, "Movecraft-Combat AutoBan");
        player.kickPlayer("Combat release!");
    }

    private void startCombat(@NotNull Player player) {
        Bukkit.getServer().getPluginManager().callEvent(new CombatStartEvent(player));
        player.sendMessage("You have now entered combat.");
        Bukkit.getLogger().info("[Movecraft-Combat] " + player.getName() + " has entered combat.");
    }

    private void stopCombat(@NotNull Player player) {
        Bukkit.getServer().getPluginManager().callEvent(new CombatStopEvent(player));
        player.sendMessage("You have now left combat.");
        Bukkit.getLogger().info("[Movecraft-Combat] " + player.getName() + " has left combat.");
    }

    private boolean isInAirspace(@NotNull Craft craft) {
        return false;
    }
}
