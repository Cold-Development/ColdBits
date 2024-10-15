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

public class BroadcastCommand extends BitsCommand {

    public BroadcastCommand() {
        super("broadcast", CommandManager.CommandAliases.BROADCAST);
    }

    @Override
    public void execute(ColdBits plugin, CommandSender sender, String[] args) {
        LocaleManager localeManager = plugin.getManager(LocaleManager.class);
        if (args.length < 1) {
            localeManager.sendMessage(sender, "command-broadcast-usage");
            return;
        }

        BitsUtils.getPlayerByName(args[0], player -> {
            if (player == null) {
                localeManager.sendMessage(sender, "unknown-player", StringPlaceholders.of("player", args[0]));
                return;
            }

            int bits = plugin.getAPI().look(player.getFirst());
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                localeManager.sendMessage(onlinePlayer, "command-broadcast-message", StringPlaceholders.builder("player", player.getSecond())
                        .add("amount", BitsUtils.formatBits(bits))
                        .add("currency", localeManager.getCurrencyName(bits)).build());
            }
        });
    }

    @Override
    public List<String> tabComplete(ColdBits plugin, CommandSender sender, String[] args) {
        return args.length != 1 ? Collections.emptyList() : BitsUtils.getPlayerTabComplete(args[0]);
    }

}
