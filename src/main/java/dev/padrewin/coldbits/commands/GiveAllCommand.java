package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveAllCommand extends BaseBitsCommand {

    public GiveAllCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context, Integer amount, String includeOffline, String silentFlag) {
        this.coldPlugin.getScheduler().runTaskAsync(() -> {
            if (includeOffline != null) {
                this.coldPlugin.getManager(DataManager.class).offsetAllBits(amount);
            } else {
                List<UUID> playerIds = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());
                this.api.giveAll(playerIds, amount);
            }

            CommandSender sender = context.getSender();
            if (silentFlag == null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    this.localeManager.sendCommandMessage(player, "command-give-received", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                            .add("currency", this.localeManager.getCurrencyName(amount))
                            .build());
                }

                this.localeManager.sendCommandMessage(sender, "command-giveall-success", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                        .add("currency", this.localeManager.getCurrencyName(amount))
                        .build());
            }
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("giveall")
                .descriptionKey("command-giveall-description")
                .permission("coldbits.giveall")
                .arguments(ArgumentsDefinition.builder()
                        .required("amount", ArgumentHandlers.INTEGER)
                        .optional("*", ArgumentHandlers.forValues(String.class, "*"))
                        .optional("-s", ArgumentHandlers.forValues(String.class, "-s"))
                        .build())
                .build();
    }

}
