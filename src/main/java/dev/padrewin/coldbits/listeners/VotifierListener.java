package dev.padrewin.coldbits.listeners;

import com.vexsoftware.votifier.model.VotifierEvent;
import dev.padrewin.colddev.utils.StringPlaceholders;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.setting.SettingKey;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VotifierListener implements Listener {

    private final ColdBits plugin;

    public VotifierListener(ColdBits plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void vote(VotifierEvent event) {
        if (event.getVote().getUsername() == null)
            return;

        String name = event.getVote().getUsername();
        BitsUtils.getPlayerByName(name, playerInfo -> {
            if (playerInfo == null)
                return;

            int amount = SettingKey.VOTE_AMOUNT.get();
            Player player = Bukkit.getPlayer(playerInfo.getFirst());

            if (!SettingKey.VOTE_ONLINE.get() || player != null) {
                this.plugin.getAPI().give(playerInfo.getFirst(), amount);
                if (player != null)
                    this.plugin.getManager(LocaleManager.class).sendMessage(player, "votifier-voted", StringPlaceholders.builder("service", event.getVote().getServiceName())
                            .add("amount", SettingKey.VOTE_AMOUNT.get())
                            .build());
            }
        });
    }
}
