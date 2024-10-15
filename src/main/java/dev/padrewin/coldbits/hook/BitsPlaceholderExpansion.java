package dev.padrewin.coldbits.hook;

import java.util.List;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.manager.LeaderboardManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.models.SortedPlayer;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.OfflinePlayer;

public class BitsPlaceholderExpansion extends PlaceholderExpansion {

    private final ColdBits coldBits;
    private final DataManager dataManager;
    private final LeaderboardManager leaderboardManager;
    private final LocaleManager localeManager;

    public BitsPlaceholderExpansion(ColdBits coldBits) {
        this.coldBits = coldBits;
        this.dataManager = this.coldBits.getManager(DataManager.class);
        this.leaderboardManager = this.coldBits.getManager(LeaderboardManager.class);
        this.localeManager = this.coldBits.getManager(LocaleManager.class);
    }

    @Override
    public String onRequest(OfflinePlayer player, String placeholder) {
        if (player != null) {
            switch (placeholder.toLowerCase()) {
                case "bits":
                    return String.valueOf(this.dataManager.getEffectiveBits(player.getUniqueId()));
                case "bits_formatted":
                    return BitsUtils.formatBits(this.dataManager.getEffectiveBits(player.getUniqueId()));
                case "bits_shorthand":
                    return BitsUtils.formatBitsShorthand(this.dataManager.getEffectiveBits(player.getUniqueId()));
                case "leaderboard_position":
                    Long position = this.leaderboardManager.getPlayerLeaderboardPosition(player.getUniqueId());
                    return String.valueOf(position != null ? position : -1);
                case "leaderboard_position_formatted":
                    try {
                        Long position1 = this.leaderboardManager.getPlayerLeaderboardPosition(player.getUniqueId());
                        return BitsUtils.formatBits(position1 != null ? position1 : -1);
                    } catch (Exception e) {
                        return null;
                    }
            }
        }

        if (placeholder.toLowerCase().startsWith("leaderboard_")) {
            try {
                String suffix = placeholder.substring("leaderboard_".length());
                int underscoreIndex = suffix.indexOf('_');
                int position;
                if (underscoreIndex != -1) {
                    String positionValue = suffix.substring(0, underscoreIndex);
                    suffix = suffix.substring(underscoreIndex + 1);
                    position = Integer.parseInt(positionValue);

                } else {
                    position = Integer.parseInt(suffix);
                    suffix = "";
                }

                List<SortedPlayer> leaderboard = this.leaderboardManager.getLeaderboard();
                if (position > leaderboard.size())
                    return this.localeManager.getLocaleMessage("leaderboard-empty-entry");

                SortedPlayer leader = leaderboard.get(position - 1);

                // Display the player's name
                if (suffix.isEmpty())
                    return leader.getUsername();

                switch (suffix.toLowerCase()) {
                    case "amount":
                        return String.valueOf(leader.getBits());
                    case "amount_formatted":
                        return BitsUtils.formatBits(leader.getBits());
                    case "amount_shorthand":
                        return BitsUtils.formatBitsShorthand(leader.getBits());
                }
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return this.coldBits.getDescription().getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return this.coldBits.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return this.coldBits.getDescription().getVersion();
    }

}
