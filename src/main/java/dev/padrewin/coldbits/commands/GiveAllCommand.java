package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.CommandManager;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveAllCommand extends BitsCommand {

    public GiveAllCommand() {
        super("giveall", CommandManager.CommandAliases.GIVEALL);
    }

    @Override
    public void execute(ColdBits plugin, CommandSender sender, String[] args) {
        LocaleManager localeManager = plugin.getManager(LocaleManager.class);
        if (args.length < 1) {
            localeManager.sendMessage(sender, "command-giveall-usage");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            localeManager.sendMessage(sender, "invalid-amount");
            return;
        }

        final boolean silent = args.length > 1 && args[args.length - 1].equalsIgnoreCase("-s");
        final boolean includeOffline = args.length > 1 && args[1].equals("*");

        plugin.getScheduler().runTaskAsync(() -> {
            boolean success;
            if (includeOffline) {
                success = plugin.getManager(DataManager.class).offsetAllBits(amount);
            } else {
                List<UUID> playerIds = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());
                success = plugin.getAPI().giveAll(playerIds, amount);
            }

            if (success) {
                if (!silent) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        localeManager.sendMessage(player, "command-give-received", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                                .add("currency", localeManager.getCurrencyName(amount))
                                .build());
                    }
                }

                localeManager.sendMessage(sender, "command-giveall-success", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                        .add("currency", localeManager.getCurrencyName(amount))
                        .build());
            }
        });
    }

    @Override
    public List<String> tabComplete(ColdBits plugin, CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return Collections.singletonList("<amount>");
            case 2:
                return Collections.singletonList("*");
            case 3:
                return Collections.singletonList("-s");
            default:
                return Collections.emptyList();
        }

    }
}
