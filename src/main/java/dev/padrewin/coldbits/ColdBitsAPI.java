package dev.padrewin.coldbits;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import dev.padrewin.coldbits.event.ColdBitsChangeEvent;
import dev.padrewin.coldbits.event.ColdBitsResetEvent;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.models.SortedPlayer;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * The API for the ColdBits plugin.
 * Used to manipulate a player's bits balance.
 *
 * Note: This API does not send any messages and changes will be saved to the database automatically.
 */
public class ColdBitsAPI {

    private final ColdBits plugin;

    public ColdBitsAPI(ColdBits plugin) {
        this.plugin = plugin;
    }

    /**
     * Gives a player a specified amount of bits
     *
     * @param playerId The player to give bits to
     * @param amount The amount of bits to give
     * @return true if the transaction was successful, false otherwise
     */
    public boolean give(@NotNull UUID playerId, int amount) {
        Objects.requireNonNull(playerId);

        ColdBitsChangeEvent event = new ColdBitsChangeEvent(playerId, amount);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        return this.plugin.getManager(DataManager.class).offsetBits(playerId, event.getChange());
    }

    /**
     * Gives a collection of players a specified amount of bits
     *
     * @param playerIds The players to give bits to
     * @param amount The amount of bits to give
     * @return true if any transaction was successful, false otherwise
     */
    @NotNull
    public boolean giveAll(@NotNull Collection<UUID> playerIds, int amount) {
        Objects.requireNonNull(playerIds);

        boolean success = false;
        for (UUID uuid : playerIds)
            success |= this.give(uuid, amount);

        return success;
    }

    /**
     * Takes a specified amount of bits from a player
     *
     * @param playerId The player to take bits from
     * @param amount The amount of bits to take
     * @return true if the transaction was successful, false otherwise
     */
    public boolean take(@NotNull UUID playerId, int amount) {
        Objects.requireNonNull(playerId);

        return this.give(playerId, -amount);
    }

    /**
     * Looks at the number of bits a player has
     *
     * @param playerId The player to give bits to
     * @return the amount of bits a player has
     */
    public int look(@NotNull UUID playerId) {
        Objects.requireNonNull(playerId);

        return this.plugin.getManager(DataManager.class).getEffectiveBits(playerId);
    }

    /**
     * Looks at the number of bits a player has formatted with number separators
     *
     * @param playerId The player to give bits to
     * @return the amount of bits a player has
     */
    public String lookFormatted(@NotNull UUID playerId) {
        Objects.requireNonNull(playerId);

        return BitsUtils.formatBits(this.plugin.getManager(DataManager.class).getEffectiveBits(playerId));
    }

    /**
     * Looks at the number of bits a player has formatted as shorthand notation
     *
     * @param playerId The player to give bits to
     * @return the amount of bits a player has
     */
    public String lookShorthand(@NotNull UUID playerId) {
        Objects.requireNonNull(playerId);

        return BitsUtils.formatBitsShorthand(this.plugin.getManager(DataManager.class).getEffectiveBits(playerId));
    }

    /**
     * Takes bits from a source player and gives them to a target player
     *
     * @param source The player to take bits from
     * @param target The player to give bits to
     * @param amount The amount of bits to take/give
     * @return true if the transaction was successful, false otherwise
     */
    public boolean pay(@NotNull UUID source, @NotNull UUID target, int amount) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        if (!this.take(source, amount))
            return false;

        if (!this.give(target, amount)) {
            this.give(source, amount);
            return false;
        }

        return true;
    }

    /**
     * Sets a player's bits to a specified amount
     *
     * @param playerId The player to set the bits of
     * @param amount The amount of bits to set to
     * @return true if the transaction was successful, false otherwise
     */
    public boolean set(@NotNull UUID playerId, int amount) {
        Objects.requireNonNull(playerId);

        DataManager dataManager = this.plugin.getManager(DataManager.class);
        int bits = dataManager.getEffectiveBits(playerId);
        ColdBitsChangeEvent event = new ColdBitsChangeEvent(playerId, amount - bits);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        return dataManager.setBits(playerId, bits + event.getChange());
    }

    /**
     * Sets a player's bits to zero
     *
     * @param playerId The player to reset the bits of
     * @return true if the transaction was successful, false otherwise
     */
    public boolean reset(@NotNull UUID playerId) {
        Objects.requireNonNull(playerId);

        ColdBitsResetEvent event = new ColdBitsResetEvent(playerId);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        return this.plugin.getManager(DataManager.class).setBits(playerId, 0);
    }

    /**
     * Gets a List of a maximum number of players sorted by the number of bits they have.
     *
     * @param limit The maximum number of players to get
     * @return a List of all players sorted by the number of bits they have.
     */
    public List<SortedPlayer> getTopSortedBits(int limit) {
        return this.plugin.getManager(DataManager.class).getTopSortedBits(limit);
    }

    /**
     * @return a List of all players sorted by the number of bits they have.
     */
    public List<SortedPlayer> getTopSortedBits() {
        return this.plugin.getManager(DataManager.class).getTopSortedBits(null);
    }

}