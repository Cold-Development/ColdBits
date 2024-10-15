package dev.padrewin.coldbits.event;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * Events extending this may run on either the main thread or an async thread as needed. Always assume it is being run
 * async, do not perform operations that should not be done in an async environment.
 */
public abstract class ColdBitsEvent extends Event implements Cancellable {

    /**
     * Player whose bits is changing.
     */
    private final UUID playerId;
    /**
     * Amount their bits are being changed by. Note, this is NOT the final
     * amount that the player's bits balance will be. This is the amount to
     * modify their current balance by.
     */
    private int change;
    /**
     * Cancelled flag.
     */
    private boolean cancelled;

    /**
     * Constructor.
     *
     * @param id     - Id of player.
     * @param change - Amount of change that will apply to their current balance.
     */
    public ColdBitsEvent(UUID id, int change) {
        super(!Bukkit.isPrimaryThread());
        this.playerId = id;
        this.change = change;
    }

    /**
     * Get the amount of bits that the player's balance will change by.
     *
     * @return Amount of change.
     */
    public int getChange() {
        return this.change;
    }

    /**
     * Set the amount of change that will be used to adjust the player's
     * balance.
     *
     * @param change - Amount of change.
     */
    public void setChange(int change) {
        this.change = change;
    }

    /**
     * Get the player id.
     *
     * @return Player UUID.
     */
    public UUID getPlayerId() {
        return this.playerId;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
