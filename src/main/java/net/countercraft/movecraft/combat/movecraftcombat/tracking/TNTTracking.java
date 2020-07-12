package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;
import net.countercraft.movecraft.combat.movecraftcombat.directors.CannonDirectorManager;


public class TNTTracking {
    private static TNTTracking instance;
    private final HashMap<TNTPrimed, Player> tracking = new HashMap<>();

    public TNTTracking() {
        instance = this;
    }

    public static TNTTracking getInstance() {
        return instance;
    }


    public void dispensedTNT(@NotNull Craft craft, @NotNull TNTPrimed tnt) {
        if(CannonDirectorManager.getInstance().hasDirector(craft))
            tracking.put(tnt, CannonDirectorManager.getInstance().getDirector(craft));
        else
            tracking.put(tnt, craft.getNotificationPlayer());
    }

    public void damagedCraft(@NotNull Craft craft, @NotNull TNTPrimed tnt) {
        Player cause = tracking.get(tnt);
        tracking.remove(tnt);

        if(cause == null)
            return;
        DamageManager.getInstance().addDamageRecord(craft, cause, DamageType.CANNON);
        StatusManager.getInstance().registerEvent(craft.getNotificationPlayer());
    }

    public void explodedTNT(@NotNull TNTPrimed tnt) {
        tracking.remove(tnt);
    }
}