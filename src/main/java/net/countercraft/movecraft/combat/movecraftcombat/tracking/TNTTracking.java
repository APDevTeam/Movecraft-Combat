package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.UUID;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;


public class TNTTracking {
    private static TNTTracking instance;

    public TNTTracking() {
        instance = this;
    }

    public static TNTTracking getInstance() {
        return instance;
    }


    public void dispensedTNT(@NotNull Craft craft, @NotNull TNTPrimed tnt) {
        Player sender;
        if(MovecraftCombat.getInstance().getCannonDirectors().hasDirector(craft))
            sender = MovecraftCombat.getInstance().getCannonDirectors().getDirector(craft);
        else
            sender = craft.getNotificationPlayer();
        if(sender == null)
            return;

        tnt.setMetadata("MCC-Sender", new FixedMetadataValue(MovecraftCombat.getInstance(), sender.getUniqueId().toString()));
    }

    public void damagedCraft(@NotNull Craft craft, @NotNull TNTPrimed tnt) {
        UUID sender = UUID.fromString(tnt.getMetadata("MCC-Sender").get(0).asString());
        Player cause = MovecraftCombat.getInstance().getServer().getPlayer(sender);

        if(cause == null || !cause.isOnline())
            return;
        DamageManager.getInstance().addDamageRecord(craft, cause, DamageType.CANNON);
        StatusManager.getInstance().registerEvent(craft.getNotificationPlayer());
    }
}