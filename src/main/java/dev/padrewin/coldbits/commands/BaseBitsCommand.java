package dev.padrewin.coldbits.commands;

import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.ColdBitsAPI;

public abstract class BaseBitsCommand extends BaseColdCommand {

    protected final ColdBitsAPI api;
    protected final LocaleManager localeManager;

    public BaseBitsCommand(ColdBits coldBits) {
        super(coldBits);
        this.api = coldBits.getAPI();
        this.localeManager = coldBits.getManager(LocaleManager.class);
    }

}
