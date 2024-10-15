package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.database.MySQLConnector;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.CommandManager;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.models.SortedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ImportCommand extends BitsCommand {

    public ImportCommand() {
        super("import", CommandManager.CommandAliases.IMPORT);
    }

    @Override
    public void execute(ColdBits plugin, CommandSender sender, String[] args) {
        LocaleManager localeManager = plugin.getManager(LocaleManager.class);
        File file = new File(plugin.getDataFolder(), "storage.yml");
        if (!file.exists()) {
            localeManager.sendMessage(sender, "command-import-no-backup");
            return;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("confirm")) {
            String databaseType = plugin.getManager(DataManager.class).getDatabaseConnector() instanceof MySQLConnector ? "MySQL" : "SQLite";
            localeManager.sendMessage(sender, "command-import-warning", StringPlaceholders.of("type", databaseType));
            return;
        }

        plugin.getScheduler().runTaskAsync(() -> {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection bitsSection = configuration.getConfigurationSection("Bits");
            if (bitsSection == null)
                bitsSection = configuration.getConfigurationSection("Players");

            if (bitsSection == null) {
                plugin.getLogger().warning("Malformed storage.yml file.");
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

            plugin.getManager(DataManager.class).importData(data, uuidMap);
            localeManager.sendMessage(sender, "command-import-success");
        });
    }

    @Override
    public List<String> tabComplete(ColdBits plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
