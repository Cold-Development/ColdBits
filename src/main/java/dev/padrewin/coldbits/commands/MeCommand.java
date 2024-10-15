package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.CommandManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MeCommand extends BitsCommand {

    public MeCommand() {
        super("me", CommandManager.CommandAliases.ME);
    }

    @Override
    public void execute(ColdBits plugin, CommandSender sender, String[] args) {
        LocaleManager localeManager = plugin.getManager(LocaleManager.class);
        if (!(sender instanceof Player)) {
            localeManager.sendMessage(sender, "no-console");
            return;
        }

        plugin.getScheduler().runTaskAsync(() -> {
            int amount = plugin.getAPI().look(((Player) sender).getUniqueId());
            localeManager.sendMessage(sender, "command-me-success", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                    .add("currency", localeManager.getCurrencyName(amount))
                    .build());
        });
    }

    @Override
    public List<String> tabComplete(ColdBits plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
