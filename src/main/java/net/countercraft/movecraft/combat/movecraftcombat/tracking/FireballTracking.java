package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.HashMap;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.directors.AADirectorManager;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.entity.Fireball;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public class FireballTracking {
    private static FireballTracking instance;
    private final HashMap<Fireball, Player> tracking = new HashMap<>();

    public FireballTracking() {
        instance = this;
    }

    public static FireballTracking getInstance() {
        return instance;
    }


    public void dispensedFireball(@NotNull Craft craft, @NotNull Fireball fireball) {
        if(!Config.EnableFireballTracking)
            return;

        if(MovecraftCombat.getInstance().getAADirectors().hasDirector(craft))
            tracking.put(fireball, MovecraftCombat.getInstance().getAADirectors().getDirector(craft));
        else
            tracking.put(fireball, craft.getNotificationPlayer());
    }

    public void damagedCraft(@NotNull Craft craft, @NotNull Fireball fireball) {
        if(!Config.EnableFireballTracking)
            return;
        Player cause = tracking.get(fireball);
        tracking.remove(fireball);
        if(cause == null)
            return;

        DamageManager.getInstance().addDamageRecord(craft, cause, DamageType.FIREBALL);
        StatusManager.getInstance().registerEvent(craft.getNotificationPlayer());
    }

    public void expiredFireball(@NotNull Fireball fireball) {
        if(!Config.EnableFireballTracking)
            return;
        tracking.remove(fireball);
    }
}
