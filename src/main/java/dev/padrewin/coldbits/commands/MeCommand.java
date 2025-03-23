package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.utils.StringPlaceholders;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MeCommand extends BaseBitsCommand {

    public MeCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        CommandSender sender = context.getSender();
        LocaleManager localeManager = this.coldPlugin.getManager(LocaleManager.class);
        int amount = this.api.look(((Player) sender).getUniqueId());
        localeManager.sendCommandMessage(sender, "command-me-success", StringPlaceholders.builder("amount", BitsUtils.formatBits(amount))
                .add("currency", localeManager.getCurrencyName(amount))
                .build());
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("me")
                .descriptionKey("command-me-description")
                .permission("coldbits.me")
                .playerOnly()
                .build();
    }

}
