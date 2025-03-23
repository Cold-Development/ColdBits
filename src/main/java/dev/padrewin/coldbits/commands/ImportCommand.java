package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.database.MySQLConnector;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.models.SortedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ImportCommand extends BaseBitsCommand {

    public ImportCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context, String confirm) {
        CommandSender sender = context.getSender();
        File file = new File(this.coldPlugin.getDataFolder(), "storage.yml");
        if (!file.exists()) {
            this.localeManager.sendMessage(sender, "command-import-no-backup");
            return;
        }

        DataManager dataManager = this.coldPlugin.getManager(DataManager.class);
        if (confirm == null) {
            String databaseType = dataManager.getDatabaseConnector() instanceof MySQLConnector ? "MySQL" : "SQLite";
            this.localeManager.sendMessage(sender, "command-import-warning", StringPlaceholders.of("type", databaseType));
            return;
        }

        this.coldPlugin.getScheduler().runTaskAsync(() -> {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection bitsSection = configuration.getConfigurationSection("Bits");
            if (bitsSection == null)
                bitsSection = configuration.getConfigurationSection("Players");

            if (bitsSection == null) {
                this.coldPlugin.getLogger().warning("Malformed storage.yml file.");
                return;
            }

            ConfigurationSection uuidSection = configuration.getConfigurationSection("UUIDs");
            Map<UUID, String> uuidMap = new HashMap<>();
            if (uuidSection != null) {
                for (String uuidString : uuidSection.getKeys(false)) {
                    String name = uuidSection.getString(uuidString);
                    UUID uuidObj = UUID.fromString(uuidString);
                    uuidMap.put(uuidObj, name);
                }
            }

            SortedSet<SortedPlayer> data = new TreeSet<>();
            for (String uuidString : bitsSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int bits = bitsSection.getInt(uuidString);

                String username = uuidMap.get(uuid);
                if (username != null) {
                    data.add(new SortedPlayer(uuid, username, bits));
                } else {
                    data.add(new SortedPlayer(uuid, bits));
                }
            }

            dataManager.importData(data, uuidMap);
            this.localeManager.sendCommandMessage(sender, "command-import-success");
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("import")
                .descriptionKey("command-import-description")
                .permission("coldbits.import")
                .arguments(ArgumentsDefinition.builder()
                        .optional("confirm", ArgumentHandlers.forValues(String.class, "confirm"))
                        .build())
                .build();
    }

}
