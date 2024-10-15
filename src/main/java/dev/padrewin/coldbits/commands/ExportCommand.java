package dev.padrewin.coldbits.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.CommandManager;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.models.SortedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ExportCommand extends BitsCommand {

    public ExportCommand() {
        super("export", CommandManager.CommandAliases.EXPORT);
    }

    @Override
    public void execute(ColdBits plugin, CommandSender sender, String[] args) {
        plugin.getScheduler().runTaskAsync(() -> {
            LocaleManager localeManager = plugin.getManager(LocaleManager.class);
            File file = new File(plugin.getDataFolder(), "storage.yml");
            if (file.exists() && (args.length < 1 || !args[0].equalsIgnoreCase("confirm"))) {
                localeManager.sendMessage(sender, "command-export-warning");
                return;
            }

            if (file.exists())
                file.delete();

            List<SortedPlayer> data = plugin.getManager(DataManager.class).getTopSortedBits(null);
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection bitsSection = configuration.createSection("Bits");
            ConfigurationSection uuidSection = configuration.createSection("UUIDs");

            for (SortedPlayer playerData : data) {
                bitsSection.set(playerData.getUniqueId().toString(), playerData.getBits());
                if (!playerData.getUsername().equalsIgnoreCase("Unknown"))
                    uuidSection.set(playerData.getUniqueId().toString(), playerData.getUsername());
            }

            try {
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            localeManager.sendMessage(sender, "command-export-success");
        });
    }

    @Override
    public List<String> tabComplete(ColdBits plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
