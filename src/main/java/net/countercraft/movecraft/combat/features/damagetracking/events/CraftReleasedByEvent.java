package net.countercraft.movecraft.combat.features.damagetracking.events;

import net.countercraft.movecraft.combat.features.damagetracking.DamageRecord;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.utils.NameUtils;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;


public class CraftReleasedByEvent extends CraftEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<DamageRecord> causes;

    public CraftReleasedByEvent(@NotNull PlayerCraft craft, @NotNull List<DamageRecord> causes) {
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
    public List<DamageRecord> getCauses() {
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

        StringBuilder sb = new StringBuilder();
        sb.append(((PlayerCraft) this.craft).getPilot().getDisplayName());
        sb.append(" ").append(I18nSupport.getInternationalisedString("Killfeed - Sunk By")).append(" ");
        sb.append(NameUtils.offlineToName(latestDamage.getCause()));
        if(players.size() < 1)
            return sb.toString();

        sb.append(" ").append(I18nSupport.getInternationalisedString("Killfeed - With Assists")).append(" ");
        for(OfflinePlayer p : players) {
            sb.append(NameUtils.offlineToName(p));
            sb.append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }
}
