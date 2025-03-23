package dev.padrewin.coldbits.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.commands.arguments.StringSuggestingArgumentHandler;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PayCommand extends BaseBitsCommand {

    private static final Cache<UUID, Long> PAY_COOLDOWN = CacheBuilder.newBuilder()
            .expireAfterWrite(500, TimeUnit.MILLISECONDS)
            .build();

    public PayCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context, String targetName, Integer amount) {
        Player player = (Player) context.getSender();
        if (PAY_COOLDOWN.getIfPresent(player.getUniqueId()) != null) {
            this.localeManager.sendCommandMessage(player, "command-cooldown");
            return;
        }

        PAY_COOLDOWN.put(player.getUniqueId(), System.currentTimeMillis());

        BitsUtils.getPlayerByName(targetName, target -> {
            if (target == null) {
                this.localeManager.sendCommandMessage(player, "unknown-player", StringPlaceholders.of("player", targetName));
                return;
            }

            if (player.getUniqueId().equals(target.getFirst())) {
                this.localeManager.sendCommandMessage(player, "command-pay-self");
                return;
            }

            if (amount <= 0) {
                this.localeManager.sendCommandMessage(player, "invalid-amount");
                return;
            }

            if (this.api.pay(player.getUniqueId(), target.getFirst(), amount)) {
                // Send success message to sender
                this.localeManager.sendCommandMessage(player, "command-pay-sent", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                        .add("currency", this.localeManager.getCurrencyName(amount))
                        .add("player", target.getSecond())
                        .build());

                // Send success message to target
                Player onlinePlayer = Bukkit.getPlayer(target.getFirst());
                if (onlinePlayer != null) {
                    this.localeManager.sendCommandMessage(onlinePlayer, "command-pay-received", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                            .add("currency", this.localeManager.getCurrencyName(amount))
                            .add("player", player.getName())
                            .build());
                }
            } else {
                this.localeManager.sendCommandMessage(player, "command-pay-lacking-funds", StringPlaceholders.of("currency", this.localeManager.getCurrencyName(0)));
            }
        });
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("pay")
                .descriptionKey("command-pay-description")
                .permission("coldbits.pay")
                .arguments(ArgumentsDefinition.builder()
                        .required("target", new StringSuggestingArgumentHandler(BitsUtils::getPlayerTabComplete))
                        .required("amount", ArgumentHandlers.INTEGER)
                        .build())
                .playerOnly()
                .build();
    }

}
