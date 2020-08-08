package net.countercraft.movecraft.combat.movecraftcombat.commands;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.countercraft.movecraft.utils.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class TracersCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!command.getName().equalsIgnoreCase("tracers"))
            return false;

        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "You must be a player to use that command.");
            return true;
        }
        Player player = (Player) commandSender;

        if(args.length == 0) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "you must specify a mode and setting or OFF.");
            return true;
        }

        String mode = args[1].toUpperCase();
        if(mode.equals("OFF")) {
            MovecraftCombat.getInstance().getPlayerManager().setMode(player, "OFF");
            return true;
        }

        if(args.length != 2) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "you must specify a mode and setting.");
            return true;
        }

        if(!mode.equals("BLOCKS") && !mode.equals("PARTICLES")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "please specify a valid mode.");
            return true;
        }

        String setting = args[2].toUpperCase();
        if(!setting.equals("LOW") && !setting.equals("MEDIUM") && !setting.equals("HIGH")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "please specify a valid setting.");
            return true;
        }

        MovecraftCombat.getInstance().getPlayerManager().setMode(player, mode + "_" + setting);
        return true;
    }
}
