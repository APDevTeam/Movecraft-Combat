package net.countercraft.movecraft.combat.features.tracers.commands;

import net.countercraft.movecraft.combat.features.tracers.config.PlayerManager;
import net.countercraft.movecraft.combat.localisation.I18nSupport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.countercraft.movecraft.util.ChatUtils.commandPrefix;

public class TNTTracerModeCommand implements TabExecutor {
    @NotNull
    private final PlayerManager manager;


    public TNTTracerModeCommand(@NotNull PlayerManager manager) {
        this.manager = manager;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] args) {
        if (!command.getName().equalsIgnoreCase("tnttracermode"))
            return false;

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(commandPrefix() + I18nSupport.getInternationalisedString("Command - Must Be Player"));
            return true;
        }
        Player player = (Player) commandSender;

        if (args.length == 0) {
            commandSender.sendMessage(commandPrefix() + I18nSupport.getInternationalisedString("Command - Current Mode") + ": " + manager.getTNTMode(player));
            return true;
        }
        if (args.length != 1) {
            commandSender.sendMessage(commandPrefix() + I18nSupport.getInternationalisedString("Command - Specify Mode"));
            return true;
        }

        String mode = args[0].toUpperCase();
        try {
            manager.setTNTMode(player, mode);
            commandSender.sendMessage(commandPrefix() + I18nSupport.getInternationalisedString("Command - Tracer Set") + ": " + mode);
            return true;
        }
        catch (IllegalArgumentException e) {
            commandSender.sendMessage(commandPrefix() + I18nSupport.getInternationalisedString("Command - Specify Valid Mode"));
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] strings) {
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
            if (!completion.startsWith(strings[strings.length - 1].toUpperCase()))
                continue;

            completions.add(completion);
        }
        return completions;
    }
}
