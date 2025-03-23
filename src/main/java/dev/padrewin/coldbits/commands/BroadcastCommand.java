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
import org.bukkit.entity.Player;

public class BroadcastCommand extends BaseBitsCommand {

    public BroadcastCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context, String target) {
        BitsUtils.getPlayerByName(target, player -> {
            if (player == null) {
                this.localeManager.sendCommandMessage(context.getSender(), "unknown-player", StringPlaceholders.of("player", target));
                return;
            }

            int bits = this.api.look(player.getFirst());
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                this.localeManager.sendCommandMessage(onlinePlayer, "command-broadcast-message", StringPlaceholders.builder("player", player.getSecond())
                        .add("amount", BitsUtils.formatBits(bits))
                        .add("currency", this.localeManager.getCurrencyName(bits)).build());
            }
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("broadcast")
                .descriptionKey("command-broadcast-description")
                .permission("coldbits.broadcast")
                .arguments(ArgumentsDefinition.builder()
                        .required("target", new StringSuggestingArgumentHandler(BitsUtils::getPlayerTabComplete))
                        .build())
                .build();
    }

}
