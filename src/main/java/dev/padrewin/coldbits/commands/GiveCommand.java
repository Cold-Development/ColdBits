package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.utils.StringPlaceholders;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.commands.arguments.StringSuggestingArgumentHandler;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCommand extends BaseBitsCommand {

    public GiveCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context, String target, Integer amount, String silentFlag) {
        BitsUtils.getPlayerByName(target, player -> {
            CommandSender sender = context.getSender();
            if (player == null) {
                this.localeManager.sendCommandMessage(sender, "unknown-player", StringPlaceholders.of("player", target));
                return;
            }

            if (amount <= 0) {
                this.localeManager.sendCommandMessage(sender, "invalid-amount");
                return;
            }

            boolean success = this.api.give(player.getFirst(), amount);

            if (success) {
                // Get new balance after giving bits
                int newBalance = this.api.look(player.getFirst());

                // Log to console regardless of silent flag
                this.localeManager.sendMessage(Bukkit.getConsoleSender(), "command-give-log", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                        .add("currency", this.localeManager.getCurrencyName(amount))
                        .add("player", player.getSecond())
                        .add("new_balance", BitsUtils.formatBits(newBalance))
                        .build());

                // Only send player notifications if not silent
                if (silentFlag == null) {
                    // Send message to receiver if they're online
                    Player onlinePlayer = Bukkit.getPlayer(player.getFirst());
                    if (onlinePlayer != null) {
                        this.localeManager.sendCommandMessage(onlinePlayer, "command-give-received", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                                .add("currency", this.localeManager.getCurrencyName(amount))
                                .build());
                    }

                    // Send message to sender
                    this.localeManager.sendCommandMessage(sender, "command-give-success", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                            .add("currency", this.localeManager.getCurrencyName(amount))
                            .add("player", player.getSecond())
                            .build());
                }
            }
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("give")
                .descriptionKey("command-give-description")
                .permission("coldbits.give")
                .arguments(ArgumentsDefinition.builder()
                        .required("target", new StringSuggestingArgumentHandler(BitsUtils::getPlayerTabComplete))
                        .required("amount", ArgumentHandlers.INTEGER)
                        .optional("-s", ArgumentHandlers.forValues(String.class, "-s"))
                        .build())
                .build();
    }

}