package net.countercraft.movecraft.combat.tracking;

import java.util.List;
import java.util.UUID;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.tracking.damagetype.TNTCannonDamage;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import net.countercraft.movecraft.craft.Craft;


public class TNTTracking {
    private static TNTTracking instance;

    public TNTTracking() {
        instance = this;
    }

    public static TNTTracking getInstance() {
        return instance;
    }


    public void dispensedTNT(@NotNull PlayerCraft craft, @NotNull TNTPrimed tnt) {
        Player sender;
        if(MovecraftCombat.getInstance().getCannonDirectors().hasDirector(craft))
            sender = MovecraftCombat.getInstance().getCannonDirectors().getDirector(craft);
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
        DamageManager.getInstance().addDamageRecord(playerCraft, cause, new TNTCannonDamage());
        StatusManager.getInstance().registerEvent(playerCraft.getPilot());
    }
}