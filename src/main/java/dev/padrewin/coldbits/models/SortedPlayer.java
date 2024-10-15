package dev.padrewin.coldbits.models;

import java.util.UUID;
import dev.padrewin.coldbits.util.NameFetcher;

/**
 * Stores information about a player and how many bits they have.
 *
 * Holder class that will sort based on the bits and by the name. Note, this
 * sorts by order of highest bits first and uses the UUID for any matches.
 *
 * @author Mitsugaru
 */
public class SortedPlayer implements Comparable<SortedPlayer> {

    private final UUID uuid;
    private final String username;
    private final int bits;

    public SortedPlayer(UUID uuid, String username, int bits) {
        this.uuid = uuid;
        this.username = username;
        this.bits = bits;
    }

    public SortedPlayer(UUID uuid, int bits) {
        this(uuid, NameFetcher.getName(uuid), bits);
    }

    /**
     * @return UUID of the player.
     */
    public UUID getUniqueId() {
        return this.uuid;
    }

    /**
     * @return Username of the player.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @return Point amount.
     */
    public int getBits() {
        return this.bits;
    }

    @Override
    public int compareTo(SortedPlayer o) {
        if (this.getBits() > o.getBits()) {
            return -1;
        } else if (this.getBits() < o.getBits()) {
            return 1;
        }
        return this.getUniqueId().compareTo(o.getUniqueId());
    }

}
