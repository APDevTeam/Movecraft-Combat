package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.features.tracking.DamageTracking;
import net.countercraft.movecraft.combat.features.tracking.FireballTracking;
import net.countercraft.movecraft.combat.features.tracking.TNTTracking;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;


@Deprecated(forRemoval = true)
public class ExplosionListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityExplodeEvent(EntityExplodeEvent e) {
        processTNTTracking(e);
        processFireballTracking(e);
   }


    private void processTNTTracking(@NotNull EntityExplodeEvent e) {
        if(!DamageTracking.EnableTNTTracking)
            return;
        if((!(e.getEntity() instanceof TNTPrimed)))
            return;

        TNTPrimed tnt = (TNTPrimed) e.getEntity();
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getLocation());
        if(craft == null)
            return;
        for(Block b : e.blockList()) {
            if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation()))) {
                TNTTracking.getInstance().damagedCraft(craft, tnt);
                return;
            }
        }
    }

    private void processFireballTracking(@NotNull EntityExplodeEvent e) {
        if(!DamageTracking.EnableFireballTracking)
            return;
        if(!(e.getEntity() instanceof Fireball))
            return;
        Fireball fireball = (Fireball) e.getEntity();
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getLocation());
        if(!(craft instanceof PlayerCraft))
            return;
        PlayerCraft playerCraft = (PlayerCraft) craft;
        if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getLocation()))) {
            FireballTracking.getInstance().damagedCraft(playerCraft, fireball);
            return;
        }
        for(Block b : e.blockList()) {
            if(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation()))) {
                FireballTracking.getInstance().damagedCraft(playerCraft, fireball);
                return;
            }
        }
    }
}
