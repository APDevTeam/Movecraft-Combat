package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import net.countercraft.movecraft.craft.Craft;


public class TNTTracking {
    private static TNTTracking instance;
    private final HashMap<TNTPrimed, Player> tracking = new HashMap<>();

    public TNTTracking() {
        instance = this;
    }

    public static TNTTracking getInstance() {
        return instance;
    }


    public void dispensedTNT(Player pilot, TNTPrimed tnt) {
        tracking.put(tnt, pilot);
    }

    public void damagedCraft(@NotNull Craft craft, @NotNull TNTPrimed tnt) {
        Player cause = tracking.get(tnt);
        tracking.remove(tnt);

        if(cause == null)
            return;
        TrackingManager.getInstance().addDamageRecord(craft, cause, DamageType.CANNON);
    }

    public void explodedTNT(@NotNull TNTPrimed tnt) {
        tracking.remove(tnt);
    }
}