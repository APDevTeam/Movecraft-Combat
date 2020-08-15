package net.countercraft.movecraft.combat.movecraftcombat.commands;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.countercraft.movecraft.utils.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class TracerModeCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!command.getName().equalsIgnoreCase("tracermode"))
            return false;

        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "You must be a player to use that command.");
            return true;
        }
        Player player = (Player) commandSender;

        if(args.length == 0) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Current mode: " + MovecraftCombat.getInstance().getPlayerManager().getMode(player));
            return true;
        }
        if(args.length != 1) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "You must specify a mode.");
            return true;
        }

        String mode = args[0].toUpperCase();
        if(!mode.equals("BLOCKS") && !mode.equals("PARTICLES")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Please specify a valid mode.");
            return true;
        }

        MovecraftCombat.getInstance().getPlayerManager().setMode(player, mode);
        commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Tracers now set to: " + mode);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] strings) {
        final List<String> tabCompletions = new ArrayList<>();
        if (strings.length <= 1) {
            tabCompletions.add("BLOCKS");
            tabCompletions.add("PARTICLES");
        }
        if (strings.length == 0) {
            return tabCompletions;
        }
        final List<String> completions = new ArrayList<>();
        for (String completion : tabCompletions) {
            if (!completion.startsWith(strings[strings.length - 1])) {
                continue;
            }
            completions.add(completion);
        }
        return completions;
    }
}
