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
        Player senderPlayer = (Player) context.getSender();

        // Verifică cooldown
        if (PAY_COOLDOWN.getIfPresent(senderPlayer.getUniqueId()) != null) {
            this.localeManager.sendCommandMessage(senderPlayer, "command-cooldown");
            return;
        }
        PAY_COOLDOWN.put(senderPlayer.getUniqueId(), System.currentTimeMillis());

        // Obține target-ul printr-o metodă asincronă (după cum e BitsUtils.getPlayerByName)
        BitsUtils.getPlayerByName(targetName, target -> {
            if (target == null) {
                this.localeManager.sendCommandMessage(senderPlayer, "unknown-player",
                        StringPlaceholders.of("player", targetName));
                return;
            }

            if (senderPlayer.getUniqueId().equals(target.getFirst())) {
                this.localeManager.sendCommandMessage(senderPlayer, "command-pay-self");
                return;
            }

            if (amount <= 0) {
                this.localeManager.sendCommandMessage(senderPlayer, "invalid-amount");
                return;
            }

            // Încearcă plata
            boolean success = this.api.pay(senderPlayer.getUniqueId(), target.getFirst(), amount);
            if (success) {
                // === Jucătorul care trimite ===
                this.localeManager.sendCommandMessage(senderPlayer, "command-pay-sent",
                        StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                                .add("currency", this.localeManager.getCurrencyName(amount))
                                .add("player", target.getSecond())
                                .build());

                // === Jucătorul care primește (dacă e online) ===
                Player onlineReceiver = Bukkit.getPlayer(target.getFirst());
                if (onlineReceiver != null) {
                    this.localeManager.sendCommandMessage(onlineReceiver, "command-pay-received",
                            StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                                    .add("currency", this.localeManager.getCurrencyName(amount))
                                    .add("player", senderPlayer.getName())
                                    .build());
                }

                // === Log în consolă ===
                int senderBalance = this.api.look(senderPlayer.getUniqueId());
                int receiverBalance = this.api.look(target.getFirst());

                this.localeManager.sendMessage(Bukkit.getConsoleSender(), "command-pay-log",
                        StringPlaceholders.builder()
                                .add("sender",  senderPlayer.getName())
                                .add("receiver", target.getSecond())
                                .add("amount",   BitsUtils.formatBits(amount))
                                .add("currency", this.localeManager.getCurrencyName(amount))
                                .add("sender_balance",   BitsUtils.formatBits(senderBalance))
                                .add("receiver_balance", BitsUtils.formatBits(receiverBalance))
                                .build());

            } else {
                // Fonduri insuficiente
                this.localeManager.sendCommandMessage(senderPlayer, "command-pay-lacking-funds",
                        StringPlaceholders.of("currency", this.localeManager.getCurrencyName(0)));
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
