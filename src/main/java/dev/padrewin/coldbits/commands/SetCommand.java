package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.utils.StringPlaceholders;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.commands.arguments.StringSuggestingArgumentHandler;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCommand extends BaseBitsCommand {

    public SetCommand(ColdBits coldBits) {
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

            if (amount < 0) {
                this.localeManager.sendCommandMessage(sender, "invalid-amount");
                return;
            }

            // Try to set the bits for the player
            boolean success = this.api.set(player.getFirst(), amount);

            if (success) {
                // Get the new balance after setting
                int newBalance = this.api.look(player.getFirst());

                // Log to console regardless of silentFlag
                this.localeManager.sendMessage(Bukkit.getConsoleSender(), "command-set-log", StringPlaceholders.builder("player", player.getSecond())
                        .add("new_balance", BitsUtils.formatBits(newBalance))
                        .add("currency", this.localeManager.getCurrencyName(newBalance))
                        .build());

                // Notify sender only if not silent
                if (silentFlag == null) {
                    this.localeManager.sendCommandMessage(sender, "command-set-success", StringPlaceholders.builder("player", player.getSecond())
                            .add("currency", this.localeManager.getCurrencyName(amount))
                            .add("amount", BitsUtils.formatBits(amount))
                            .build());
                }
            }
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("set")
                .descriptionKey("command-set-description")
                .permission("coldbits.set")
                .arguments(ArgumentsDefinition.builder()
                        .required("target", new StringSuggestingArgumentHandler(BitsUtils::getPlayerTabComplete))
                        .required("amount", ArgumentHandlers.INTEGER)
                        .optional("-s", ArgumentHandlers.forValues(String.class, "-s"))
                        .build())
                .build();
    }

}