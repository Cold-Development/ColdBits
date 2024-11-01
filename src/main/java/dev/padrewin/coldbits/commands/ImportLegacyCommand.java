package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.database.MySQLConnector;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.CommandManager;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import org.bukkit.command.CommandSender;

public class ImportLegacyCommand extends BitsCommand {

    public ImportLegacyCommand() {
        super("importlegacy", CommandManager.CommandAliases.IMPORTLEGACY);
    }

    @Override
    public void execute(ColdBits plugin, CommandSender sender, String[] args) {
        plugin.getScheduler().runTaskAsync(() -> {
            LocaleManager localeManager = plugin.getManager(LocaleManager.class);
            if (!(plugin.getManager(DataManager.class).getDatabaseConnector() instanceof MySQLConnector)) {
                localeManager.sendMessage(sender, "command-importlegacy-only-mysql");
                return;
            }

            if (args.length < 1) {
                localeManager.sendMessage(sender, "command-importlegacy-usage");
                return;
            }

            if (plugin.getManager(DataManager.class).importLegacyTable(args[0])) {
                localeManager.sendMessage(sender, "command-importlegacy-success", StringPlaceholders.of("table", args[0]));
            } else {
                localeManager.sendMessage(sender, "command-importlegacy-failure", StringPlaceholders.of("table", args[0]));
            }
        });
    }

    @Override
    public List<String> tabComplete(ColdBits plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
