package dev.padrewin.coldbits.commands;

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

public class ResetCommand extends BaseBitsCommand {

    public ResetCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context, String target) {
        BitsUtils.getPlayerByName(target, player -> {
            CommandSender sender = context.getSender();
            if (player == null) {
                this.localeManager.sendCommandMessage(sender, "unknown-player", StringPlaceholders.of("player", target));
                return;
            }

            // Get old balance before reset (for logging purposes)
            int oldBalance = this.api.look(player.getFirst());

            if (this.api.reset(player.getFirst())) {
                // Get new balance after reset
                int newBalance = this.api.look(player.getFirst());

                // Send success message to command sender
                this.localeManager.sendCommandMessage(sender, "command-reset-success", StringPlaceholders.builder("player", player.getSecond())
                        .add("currency", this.localeManager.getCurrencyName(0))
                        .build());

                // Log to console
                this.localeManager.sendMessage(Bukkit.getConsoleSender(), "command-reset-log", StringPlaceholders.builder("player", player.getSecond())
                        .add("new_balance", BitsUtils.formatBits(newBalance))
                        .add("currency", this.localeManager.getCurrencyName(newBalance))
                        .build());
            }
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("reset")
                .descriptionKey("command-reset-description")
                .permission("coldbits.reset")
                .arguments(ArgumentsDefinition.builder()
                        .required("target", new StringSuggestingArgumentHandler(BitsUtils::getPlayerTabComplete))
                        .build())
                .build();
    }

}