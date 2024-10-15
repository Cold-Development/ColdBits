package dev.padrewin.coldbits.commands;

import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;
import dev.padrewin.coldbits.manager.CommandManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class SetCommand extends BitsCommand {

    public SetCommand() {
        super("set", CommandManager.CommandAliases.SET);
    }

    @Override
    public void execute(ColdBits plugin, CommandSender sender, String[] args) {
        LocaleManager localeManager = plugin.getManager(LocaleManager.class);
        if (args.length < 2) {
            localeManager.sendMessage(sender, "command-set-usage");
            return;
        }

        BitsUtils.getPlayerByName(args[0], player -> {
            if (player == null) {
                localeManager.sendMessage(sender, "unknown-player", StringPlaceholders.of("player", args[0]));
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 0) {
                    localeManager.sendMessage(sender, "invalid-amount");
                    return;
                }
            } catch (NumberFormatException e) {
                localeManager.sendMessage(sender, "invalid-amount");
                return;
            }

            if (plugin.getAPI().set(player.getFirst(), amount)) {
                int newBalance = plugin.getAPI().look(player.getFirst());

                localeManager.sendMessage(sender, "command-set-success", StringPlaceholders.builder("player", player.getSecond())
                        .add("currency", localeManager.getCurrencyName(amount))
                        .add("amount", BitsUtils.formatBits(amount))
                        .build());

                localeManager.sendMessage(Bukkit.getConsoleSender(), "command-set-log", StringPlaceholders.builder("player", player.getSecond())
                        .add("new_balance", BitsUtils.formatBits(newBalance))
                        .add("currency", localeManager.getCurrencyName(newBalance))
                        .build());
            }
        });
    }

    @Override
    public List<String> tabComplete(ColdBits plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return BitsUtils.getPlayerTabComplete(args[0]);
        } else if (args.length == 2) {
            return Collections.singletonList("<amount>");
        } else {
            return Collections.emptyList();
        }
    }

}
