package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.CommandManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.command.CommandSender;

public class LookCommand extends BitsCommand {

    public LookCommand() {
        super("look", CommandManager.CommandAliases.LOOK);
    }

    @Override
    public void execute(ColdBits plugin, CommandSender sender, String[] args) {
        LocaleManager localeManager = plugin.getManager(LocaleManager.class);
        if (args.length < 1) {
            localeManager.sendMessage(sender, "command-look-usage");
            return;
        }

        BitsUtils.getPlayerByName(args[0], player -> {
            if (player == null) {
                localeManager.sendMessage(sender, "unknown-player", StringPlaceholders.of("player", args[0]));
                return;
            }

            int amount = plugin.getAPI().look(player.getFirst());
            localeManager.sendMessage(sender, "command-look-success", StringPlaceholders.builder("player", player.getSecond())
                    .add("amount", BitsUtils.formatBits(amount))
                    .add("currency", localeManager.getCurrencyName(amount))
                    .build());
        });
    }

    @Override
    public List<String> tabComplete(ColdBits plugin, CommandSender sender, String[] args) {
        return args.length == 1 ? BitsUtils.getPlayerTabComplete(args[0]) : Collections.emptyList();
    }

}
