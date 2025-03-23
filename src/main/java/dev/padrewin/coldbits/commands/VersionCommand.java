package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.LocaleManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class VersionCommand extends BaseBitsCommand {

    public VersionCommand(ColdBits coldBits) {
        super(coldBits);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        CommandSender sender = context.getSender();
        LocaleManager localeManager = this.coldPlugin.getManager(LocaleManager.class);

        String baseColor = localeManager.getLocaleMessage("base-command-color");

        // Plugin name and version - using ColdBits colors
        localeManager.sendCustomMessage(sender, baseColor + "Running <g:#635AA7:#E6D4F8:#9E48F6>ColdBits" + baseColor +
                " v" + this.coldPlugin.getDescription().getVersion());

        // Display authors
        if (this.coldPlugin.getDescription().getAuthors().size() >= 2) {
            localeManager.sendCustomMessage(sender, baseColor + "Developer(s): <g:#FF0000:#793434>"
                    + this.coldPlugin.getDescription().getAuthors().get(0) + baseColor + " & <g:#969696:#5C5C5C>"
                    + this.coldPlugin.getDescription().getAuthors().get(1));
        } else if (!this.coldPlugin.getDescription().getAuthors().isEmpty()) {
            localeManager.sendCustomMessage(sender, baseColor + "Developer(s): <g:#FF0000:#793434>"
                    + this.coldPlugin.getDescription().getAuthors().get(0));
        }

        // Add GitHub link - custom feature from ColdBits
        if (sender instanceof Player) {
            Player player = (Player) sender;
            TextComponent baseMessage = new TextComponent(baseColor + "GitHub: ");
            TextComponent clickableText = new TextComponent(ChatColor.RED + "" + ChatColor.UNDERLINE + "click here");
            clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Cold-Development/ColdBits"));
            baseMessage.addExtra(clickableText);
            player.spigot().sendMessage(baseMessage);
        } else if (sender instanceof ConsoleCommandSender) {
            String ansiRed = "\u001B[31m";
            String ansiReset = "\u001B[0m";
            String ansiAqua = "\u001B[36m";
            sender.sendMessage(ansiAqua + "GitHub: " + ansiRed + "https://github.com/Cold-Development/ColdBits" + ansiReset);
        }

        // Help message
        localeManager.sendSimpleMessage(sender, "base-command-help");
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("version")
                .descriptionKey("command-version-description")
                .permission("coldbits.version")
                .build();
    }
}