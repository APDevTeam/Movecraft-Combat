package net.countercraft.movecraft.combat.features.damagetracking;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.config.Config;
import net.countercraft.movecraft.combat.features.AADirectors;
import net.countercraft.movecraft.combat.features.damagetracking.types.FireballDamage;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FireballTracking {
    @Nullable @Deprecated(forRemoval = true)
    private static FireballTracking instance;

    @Nullable @Deprecated(forRemoval = true)
    public static FireballTracking getInstance() {
        return instance;
    }


    public FireballTracking() {
        instance = this;
    }

    public void dispensedFireball(@NotNull PlayerCraft craft, @NotNull Fireball fireball) {
        if(!Config.EnableFireballTracking)
            return;
        Player sender;
        if(AADirectors.getInstance() != null && AADirectors.getInstance().hasDirector(craft))
            sender = AADirectors.getInstance().getDirector(craft);
        else
            sender = craft.getPilot();

        if(sender == null)
            return;

        fireball.setMetadata("MCC-Sender", new FixedMetadataValue(MovecraftCombat.getInstance(), sender.getUniqueId().toString()));
    }

    public void damagedCraft(@NotNull PlayerCraft craft, @NotNull Fireball fireball) {
        if(!Config.EnableFireballTracking)
            return;

        List<MetadataValue> meta = fireball.getMetadata("MCC-Sender");
        if(meta.isEmpty())
            return;

        UUID sender = UUID.fromString(meta.get(0).asString());
        Player cause = MovecraftCombat.getInstance().getServer().getPlayer(sender);
        if(cause == null || !cause.isOnline())
            return;

        DamageManager.getInstance().addDamageRecord(craft, cause, new FireballDamage());
        StatusManager.getInstance().registerEvent(craft.getPilot());
    }
}
