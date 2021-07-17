package net.countercraft.movecraft.combat.tracking;

import java.util.List;
import java.util.UUID;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.tracking.damagetype.FireballDamage;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.entity.Fireball;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.combat.config.Config;


public class FireballTracking {
    private static FireballTracking instance;

    public FireballTracking() {
        instance = this;
    }

    public static FireballTracking getInstance() {
        return instance;
    }


    public void dispensedFireball(@NotNull PlayerCraft craft, @NotNull Fireball fireball) {
        if(!Config.EnableFireballTracking)
            return;
        Player sender;
        if(MovecraftCombat.getInstance().getAADirectors().hasDirector(craft)) {
            sender = MovecraftCombat.getInstance().getAADirectors().getDirector(craft);
        }
        else {
            sender = craft.getPlayer();
        }
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
        StatusManager.getInstance().registerEvent(craft.getPlayer());
    }
}
