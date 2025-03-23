package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.models.SortedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ExportCommand extends BaseBitsCommand {

    public ExportCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context, String confirm) {
        this.coldPlugin.getScheduler().runTaskAsync(() -> {
            CommandSender sender = context.getSender();
            File file = new File(this.coldPlugin.getDataFolder(), "storage.yml");
            if (file.exists() && confirm == null) {
                this.localeManager.sendCommandMessage(sender, "command-export-warning");
                return;
            }

            if (file.exists())
                file.delete();

            List<SortedPlayer> data = this.coldPlugin.getManager(DataManager.class).getTopSortedBits(null);
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

            this.localeManager.sendCommandMessage(sender, "command-export-success");
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("export")
                .descriptionKey("command-export-description")
                .permission("coldbits.export")
                .arguments(ArgumentsDefinition.builder()
                        .optional("confirm", ArgumentHandlers.forValues(String.class, "confirm"))
                        .build())
                .build();
    }

}
