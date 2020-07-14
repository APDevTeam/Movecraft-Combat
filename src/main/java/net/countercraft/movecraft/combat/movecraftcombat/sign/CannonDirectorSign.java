package net.countercraft.movecraft.combat.movecraftcombat.sign;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;
import static net.countercraft.movecraft.utils.ChatUtils.ERROR_PREFIX;


public class CannonDirectorSign implements Listener {
    private static final String HEADER = "Cannon Director";

    @EventHandler
    public final void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
            return;
        }
        Sign sign = (Sign) event.getClickedBlock().getState();
        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(HEADER)) {
            return;
        }

        Player player = event.getPlayer();
        Craft foundCraft = null;
        for (Craft tcraft : CraftManager.getInstance().getCraftsInWorld(block.getWorld())) {
            if (tcraft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(block.getLocation())) &&
                    CraftManager.getInstance().getPlayerFromCraft(tcraft) != null) {
                foundCraft = tcraft;
                break;
            }
        }
        if (foundCraft == null) {
            player.sendMessage(ERROR_PREFIX + "Sign must be part of a piloted craft!");
            return;
        }

        if (!Config.CannonDirectorsAllowed.contains(foundCraft.getType())) {
            player.sendMessage(ERROR_PREFIX + "Cannon Director signs not allowed on this craft!");
            return;
        }

        if(event.getAction() == Action.LEFT_CLICK_BLOCK && MovecraftCombat.getInstance().getCannonDirectors().isDirector(player)){
            MovecraftCombat.getInstance().getCannonDirectors().removeDirector(event.getPlayer());
            player.sendMessage("Cannon Director signs not allowed on this craft!");
            return;
        }

        MovecraftCombat.getInstance().getCannonDirectors().addDirector(foundCraft, event.getPlayer());
        player.sendMessage("You are now directing the cannons of this craft");
        if (MovecraftCombat.getInstance().getAADirectors().isDirector(player)) {
            MovecraftCombat.getInstance().getAADirectors().removeDirector(player);
        }
    }
}
