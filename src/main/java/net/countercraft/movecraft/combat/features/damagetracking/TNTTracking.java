package net.countercraft.movecraft.combat.features.damagetracking;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.features.CannonDirectors;
import net.countercraft.movecraft.combat.features.CombatRelease;
import net.countercraft.movecraft.combat.features.damagetracking.types.TNTCannonDamage;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TNTTracking {
    @Nullable @Deprecated(forRemoval = true)
    private static TNTTracking instance = null;

    @Nullable @Deprecated(forRemoval = true)
    public static TNTTracking getInstance() {
        return instance;
    }


    @NotNull
    private final DamageTracking manager;

    public TNTTracking(@NotNull DamageTracking manager) {
        this.manager = manager;
        instance = this;
    }

    public void dispensedTNT(@NotNull PlayerCraft craft, @NotNull TNTPrimed tnt) {
        Player sender;
        if(CannonDirectors.getInstance().hasDirector(craft))
            sender = CannonDirectors.getInstance().getDirector(craft);
        else
            sender = craft.getPilot();
        if(sender == null)
            return;

        tnt.setMetadata("MCC-Sender", new FixedMetadataValue(MovecraftCombat.getInstance(), sender.getUniqueId().toString()));
    }

    public void damagedCraft(@NotNull Craft craft, @NotNull TNTPrimed tnt) {
        List<MetadataValue> meta = tnt.getMetadata("MCC-Sender");
        if(meta.isEmpty())
            return;

        UUID sender = UUID.fromString(meta.get(0).asString());
        Player cause = MovecraftCombat.getInstance().getServer().getPlayer(sender);

        if(cause == null || !cause.isOnline() || !(craft instanceof PlayerCraft))
            return;
        PlayerCraft playerCraft = (PlayerCraft) craft;
        manager.addDamageRecord(playerCraft, cause, new TNTCannonDamage());
        CombatRelease.getInstance().registerEvent(playerCraft.getPilot());
    }
}
