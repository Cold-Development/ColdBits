package dev.padrewin.coldbits.event;

import java.util.UUID;
import org.bukkit.event.HandlerList;

/**
 * Called when a player's bits is to be changed.
 */
public class ColdBitsChangeEvent extends ColdBitsEvent {

    /**
     * Handler list.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Constructor.
     *
     * @param playerId Player UUID
     * @param change   Amount of bits to be changed.
     */
    public ColdBitsChangeEvent(UUID playerId, int change) {
        super(playerId, change);
    }

    /**
     * Static method to get HandlerList.
     *
     * @return HandlerList.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
