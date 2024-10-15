package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.CommandManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCommand extends BitsCommand {

    public GiveCommand() {
        super("give", CommandManager.CommandAliases.GIVE);
    }

    @Override
    public void execute(ColdBits plugin, CommandSender sender, String[] args) {
        LocaleManager localeManager = plugin.getManager(LocaleManager.class);
        if (args.length < 2) {
            localeManager.sendMessage(sender, "command-give-usage");
            return;
        }

        boolean silent = args.length > 2 && args[args.length - 1].equalsIgnoreCase("-s");

        BitsUtils.getPlayerByName(args[0], player -> {
            if (player == null) {
                localeManager.sendMessage(sender, "unknown-player", StringPlaceholders.of("player", args[0]));
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                localeManager.sendMessage(sender, "invalid-amount");
                return;
            }

            if (amount <= 0) {
                localeManager.sendMessage(sender, "invalid-amount");
                return;
            }

            if (plugin.getAPI().give(player.getFirst(), amount)) {
                Player onlinePlayer = Bukkit.getPlayer(player.getFirst());
                if (onlinePlayer != null && !silent) {
                    localeManager.sendMessage(onlinePlayer, "command-give-received", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                            .add("currency", localeManager.getCurrencyName(amount))
                            .build());
                }

                int newBalance = plugin.getAPI().look(player.getFirst());

                localeManager.sendMessage(sender, "command-give-success", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                        .add("currency", localeManager.getCurrencyName(amount))
                        .add("player", player.getSecond())
                        .build());

                localeManager.sendMessage(Bukkit.getConsoleSender(), "command-give-log", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                        .add("currency", localeManager.getCurrencyName(amount))
                        .add("player", player.getSecond())
                        .add("new_balance", BitsUtils.formatBits(newBalance))
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
        } else if (args.length == 3) {
            return Collections.singletonList("-s");
        } else {
            return Collections.emptyList();
        }
    }

}
