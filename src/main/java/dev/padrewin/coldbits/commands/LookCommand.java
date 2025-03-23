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

public class LookCommand extends BaseBitsCommand {

    public LookCommand(ColdBits coldBits) {
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

            int amount = this.api.look(player.getFirst());
            this.localeManager.sendCommandMessage(sender, "command-look-success", StringPlaceholders.builder("player", player.getSecond())
                    .add("amount", BitsUtils.formatBits(amount))
                    .add("currency", this.localeManager.getCurrencyName(amount))
                    .build());
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("look")
                .descriptionKey("command-look-description")
                .permission("coldbits.look")
                .arguments(ArgumentsDefinition.builder()
                        .required("target", new StringSuggestingArgumentHandler(BitsUtils::getPlayerTabComplete))
                        .build())
                .build();
    }

}
