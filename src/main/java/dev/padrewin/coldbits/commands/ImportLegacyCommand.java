package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.database.MySQLConnector;
import dev.padrewin.colddev.utils.StringPlaceholders;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.DataManager;
import org.bukkit.command.CommandSender;

public class ImportLegacyCommand extends BaseBitsCommand {

    public ImportLegacyCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context, String tableName) {
        CommandSender sender = context.getSender();
        DataManager dataManager = this.coldPlugin.getManager(DataManager.class);
        if (!(dataManager.getDatabaseConnector() instanceof MySQLConnector)) {
            this.localeManager.sendCommandMessage(sender, "command-importlegacy-only-mysql");
            return;
        }

        this.coldPlugin.getScheduler().runTaskAsync(() -> {
            if (dataManager.importLegacyTable(tableName)) {
                this.localeManager.sendCommandMessage(sender, "command-importlegacy-success", StringPlaceholders.of("table", tableName));
            } else {
                this.localeManager.sendCommandMessage(sender, "command-importlegacy-failure", StringPlaceholders.of("table", tableName));
            }
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("importlegacy")
                .descriptionKey("command-importlegacy-description")
                .permission("coldbits.importlegacy")
                .arguments(ArgumentsDefinition.builder()
                        .required("tableName", ArgumentHandlers.STRING)
                        .build())
                .build();
    }

}
