package dev.padrewin.coldbits.util;

import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.colddev.ColdPlugin;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.models.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.StringUtil;

public final class BitsUtils {

    private static NumberFormat formatter = NumberFormat.getInstance();
    private static String decimal;
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    /**
     * Formats a number from 1100 to 1,100
     *
     * @param bits The bits value to format
     * @return The formatted shorthand value
     */
    public static String formatBits(long bits) {
        if (formatter != null) {
            return formatter.format(bits);
        } else {
            return String.valueOf(bits);
        }
    }

    /**
     * @return Gets the decimal separator for the shorthand bits format
     */
    public static char getDecimalSeparator() {
        if (decimal == null || decimal.trim().isEmpty())
            return '.';
        return decimal.charAt(0);
    }

    /**
     * Formats a number from 1100 to 1.1k
     * Adapted from <a>https://stackoverflow.com/questions/4753251/how-to-go-about-formatting-1200-to-1-2k-in-java</a>
     *
     * @param bits The bits value to format
     * @return The formatted shorthand value
     */
    public static String formatBitsShorthand(long bits) {
        if (bits == Long.MIN_VALUE) return formatBitsShorthand(Long.MIN_VALUE + 1);
        if (bits < 0) return "-" + formatBitsShorthand(-bits);
        if (bits < 1000) return Long.toString(bits);

        Map.Entry<Long, String> entry = suffixes.floorEntry(bits);
        Long divideBy = entry.getKey();
        String suffix = entry.getValue();

        long truncated = bits / (divideBy / 10);
        return ((truncated / 10D) + suffix).replaceFirst(Pattern.quote("."), getDecimalSeparator() + "");
    }

    public static void setCachedValues(ColdPlugin coldPlugin) {
        LocaleManager localeManager = coldPlugin.getManager(LocaleManager.class);

        String separator = localeManager.getLocaleMessage("currency-separator");
        DecimalFormat decimalFormat = new DecimalFormat();
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        if (!separator.isEmpty()) {
            symbols.setGroupingSeparator(separator.charAt(0));
            decimalFormat.setGroupingUsed(true);
            decimalFormat.setGroupingSize(3);
            decimalFormat.setDecimalFormatSymbols(symbols);
            formatter = decimalFormat;
        } else {
            formatter = null;
        }

        suffixes.clear();
        suffixes.put(1_000L, localeManager.getLocaleMessage("number-abbreviation-thousands"));
        suffixes.put(1_000_000L, localeManager.getLocaleMessage("number-abbreviation-millions"));
        suffixes.put(1_000_000_000L, localeManager.getLocaleMessage("number-abbreviation-billions"));
        decimal = localeManager.getLocaleMessage("currency-decimal");
    }

    /**
     * Gets an OfflinePlayer by name, prioritizing online players.
     *
     * @param name The name of the player
     * @param callback A callback to run with a tuple of the player's UUID and name, or null if not found
     */
    @SuppressWarnings("deprecation")
    public static void getPlayerByName(String name, Consumer<Tuple<UUID, String>> callback) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            callback.accept(new Tuple<>(player.getUniqueId(), player.getName()));
            return;
        }

        ColdBits plugin = ColdBits.getInstance();
        plugin.getScheduler().runTaskAsync(() -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            if (offlinePlayer.getName() != null && offlinePlayer.hasPlayedBefore()) {
                Tuple<UUID, String> tuple = new Tuple<>(offlinePlayer.getUniqueId(), offlinePlayer.getName());
                plugin.getScheduler().runTask(() -> callback.accept(tuple));
                return;
            }

            UUID uuid = plugin.getManager(DataManager.class).lookupCachedUUID(name);
            if (uuid != null) {
                Tuple<UUID, String> tuple = new Tuple<>(uuid, name);
                plugin.getScheduler().runTask(() -> callback.accept(tuple));
                return;
            }

            plugin.getScheduler().runTask(() -> callback.accept(null));
        });
    }

    /**
     * Gets an OfflinePlayer by name, prioritizing online players.
     * Warning: This method can cause a blocking call to the database for UUID lookups.
     *
     * @param name The name of the player
     * @return a tuple of the player's UUID and name, or null if not found
     */
    public static Tuple<UUID, String> getPlayerByName(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null)
            return new Tuple<>(player.getUniqueId(), player.getName());

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.getName() != null && offlinePlayer.hasPlayedBefore())
            return new Tuple<>(offlinePlayer.getUniqueId(), offlinePlayer.getName());

        UUID uuid = ColdBits.getInstance().getManager(DataManager.class).lookupCachedUUID(name);
        if (uuid != null)
            return new Tuple<>(uuid, name);

        return null;
    }

    /**
     * Gets a list of player names to show in tab completions, vanished players are excluded.
     *
     * @param arg The argument for the name
     * @return a list of online players excluding the
     */
    public static List<String> getPlayerTabComplete(String arg) {
        List<String> players = Bukkit.getOnlinePlayers().stream()
                .filter(x -> x.getMetadata("vanished").stream().noneMatch(MetadataValue::asBoolean))
                .map(Player::getName)
                .collect(Collectors.toList());
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(arg, players, completions);
        return completions;
    }

}
