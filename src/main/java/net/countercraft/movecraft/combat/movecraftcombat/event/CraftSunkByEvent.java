package net.countercraft.movecraft.combat.movecraftcombat.event;

import net.countercraft.movecraft.combat.movecraftcombat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageRecord;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashSet;


public class CraftSunkByEvent extends CraftEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final HashSet<DamageRecord> causes;

    public CraftSunkByEvent(@NotNull Craft craft, @NotNull HashSet<DamageRecord> causes) {
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
    public HashSet<DamageRecord> getCauses() {
        return this.causes;
    }

    @Nullable
    public DamageRecord getLastRecord() {
        DamageRecord latestDamage = null;
        for(DamageRecord r : this.causes) {
            if(latestDamage == null || r.getTime() > latestDamage.getTime())
                latestDamage = r;
        }
        assert latestDamage != null;
        return latestDamage;
    }

    @NotNull
    public String causesToString() {
        DamageRecord latestDamage = getLastRecord();
        HashSet<Player> players = new HashSet<>();
        for(DamageRecord r : this.causes) {
            players.add(r.getCause());
        }
        assert latestDamage != null;
        players.remove(latestDamage.getCause());

        StringBuilder stringBuilder = new StringBuilder();
        assert this.craft.getNotificationPlayer() != null;
        stringBuilder.append(this.craft.getNotificationPlayer().getDisplayName());
        stringBuilder.append(" ").append(I18nSupport.getInternationalisedString("Killfeed - Sunk By")).append(" ");
        stringBuilder.append(latestDamage.getCause().getDisplayName());
        if(players.size() < 1)
            return stringBuilder.toString();

        stringBuilder.append(" ").append(I18nSupport.getInternationalisedString("Killfeed - With Assists")).append(" ");
        for(Player p : players) {
            stringBuilder.append(p.getDisplayName());
            stringBuilder.append(", ");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }
}
