package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.HelpCommand;
import dev.padrewin.colddev.command.PrimaryCommand;
import dev.padrewin.colddev.command.ReloadCommand;
import dev.padrewin.colddev.command.framework.Argument;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.ColdCommand;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import java.util.Optional;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.setting.SettingKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BaseCommand extends PrimaryCommand {

    private final ColdBits coldBits;

    public BaseCommand(ColdBits coldBits) {
        super(coldBits);
        this.coldBits = coldBits;
    }

    @ColdExecutable
    @Override
    public void execute(CommandContext context) {
        String baseRedirect = SettingKey.BASE_COMMAND_REDIRECT.get();
        if (baseRedirect.trim().isEmpty())
            baseRedirect = "me";

        CommandSender sender = context.getSender();
        Optional<ColdCommand> subcommand = this.findCommand(sender, baseRedirect);
        if (subcommand.isPresent()) {
            subcommand.get().invoke(context);
        } else {
            this.findCommand(sender, "help").ifPresent(x -> x.invoke(context));
        }
    }

    private Optional<ColdCommand> findCommand(CommandSender sender, String name) {
        Argument.SubCommandArgument argument = (Argument.SubCommandArgument) this.getCommandArguments().get(0);
        return argument.subCommands().stream()
                .filter(x -> x.getName().equalsIgnoreCase(name) || x.getAliases().stream().anyMatch(y -> y.equalsIgnoreCase(name)))
                .findFirst()
                .filter(x -> !x.isPlayerOnly() || sender instanceof Player);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("bits")
                .aliases("bits")
                .permission("coldbits.basecommand")
                .arguments(ArgumentsDefinition.builder()
                        .optionalSub(
                                new BroadcastCommand(this.coldBits),
                                new ExportCommand(this.coldBits),
                                new GiveAllCommand(this.coldBits),
                                new GiveCommand(this.coldBits),
                                new HelpCommand(this.coldBits, this),
                                new ImportCommand(this.coldBits),
                                new ImportLegacyCommand(this.coldBits),
                                new LeadCommand(this.coldBits),
                                new LookCommand(this.coldBits),
                                new MeCommand(this.coldBits),
                                new PayCommand(this.coldBits),
                                new ReloadCommand(this.coldBits),
                                new ResetCommand(this.coldBits),
                                new SetCommand(this.coldBits),
                                new TakeCommand(this.coldBits),
                                new VersionCommand(this.coldBits)
                        ))
                .build();
    }

}
