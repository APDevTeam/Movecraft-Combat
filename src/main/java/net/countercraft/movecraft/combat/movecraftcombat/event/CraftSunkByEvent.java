package net.countercraft.movecraft.combat.movecraftcombat.event;

import net.countercraft.movecraft.combat.movecraftcombat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageRecord;
import net.countercraft.movecraft.combat.movecraftcombat.utils.NameUtils;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;


public class CraftSunkByEvent extends CraftEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ArrayList<DamageRecord> causes;

    public CraftSunkByEvent(@NotNull PlayerCraft craft, @NotNull ArrayList<DamageRecord> causes) {
        super(craft);
        this.causes = causes;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Nullable
    public ArrayList<DamageRecord> getCauses() {
        return this.causes;
    }

    @Nullable
    public DamageRecord getLastRecord() {
        return causes.get(causes.size() - 1);
    }

    @NotNull
    public String causesToString() {
        DamageRecord latestDamage = getLastRecord();
        HashSet<OfflinePlayer> players = new HashSet<>();
        for(DamageRecord r : this.causes) {
            players.add(r.getCause());
        }
        assert latestDamage != null;
        players.remove(latestDamage.getCause());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(((PlayerCraft) this.craft).getPlayer().getDisplayName());
        stringBuilder.append(" ").append(I18nSupport.getInternationalisedString("Killfeed - Sunk By")).append(" ");
        stringBuilder.append(NameUtils.offlineToName(latestDamage.getCause()));
        if(players.size() < 1)
            return stringBuilder.toString();

        stringBuilder.append(" ").append(I18nSupport.getInternationalisedString("Killfeed - With Assists")).append(" ");
        for(OfflinePlayer p : players) {
            stringBuilder.append(NameUtils.offlineToName(p));
            stringBuilder.append(", ");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }
}
