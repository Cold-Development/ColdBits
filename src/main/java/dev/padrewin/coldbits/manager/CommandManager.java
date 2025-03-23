package dev.padrewin.coldbits.manager;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.manager.AbstractCommandManager;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.commands.BaseCommand;

public class CommandManager extends AbstractCommandManager {

    public CommandManager(ColdPlugin coldPlugin) {
        super(coldPlugin);
    }

    @Override
    public List<Function<ColdPlugin, BaseColdCommand>> getRootCommands() {
        return Collections.singletonList(plugin -> new BaseCommand(ColdBits.getInstance()));
    }

}
