package net.countercraft.movecraft.combat.movecraftcombat.status;

import java.util.Date;
import java.util.HashMap;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
        for(Player player : records.keySet()) {
            if(records.get(player) < Config.DamageTimeout)
                continue;
            records.remove(player);
            stopCombat(player);
        }
    }


    public boolean isInCombat(Player player) {
        if(!records.containsKey(player))
            return false;
        return System.currentTimeMillis() - records.get(player) < Config.DamageTimeout;
    }

    public void registerEvent(@Nullable Player player) {
        if(player == null)
            return;
        if(!records.containsKey(player) || records.get(player) > Config.DamageTimeout)
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
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You have now entered combat."));
    }

    private void stopCombat(@NotNull Player player) {
        Bukkit.getServer().getPluginManager().callEvent(new CombatStopEvent(player));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You have now left combat."));
    }

    private boolean isInAirspace(@NotNull Craft craft) {
        return false;
    }
}
