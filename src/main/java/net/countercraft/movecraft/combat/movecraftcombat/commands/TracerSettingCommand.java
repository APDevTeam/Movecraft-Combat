package net.countercraft.movecraft.combat.movecraftcombat.commands;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.countercraft.movecraft.utils.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class TracerSettingCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!command.getName().equalsIgnoreCase("tracersetting"))
            return false;

        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "You must be a player to use that command.");
            return true;
        }
        Player player = (Player) commandSender;

        if(args.length == 0) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Current setting: " + MovecraftCombat.getInstance().getPlayerManager().getSetting(player));
            return true;
        }
        if(args.length != 1) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "You must specify a setting.");
            return true;
        }

        String setting = args[0].toUpperCase();
        if(!setting.equals("OFF") && !setting.equals("LOW") && !setting.equals("MEDIUM") && !setting.equals("HIGH")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Please specify a valid setting.");
            return true;
        }

        MovecraftCombat.getInstance().getPlayerManager().setSetting(player, setting);
        commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Tracers now set to: " + setting);
        return true;
    }
}
