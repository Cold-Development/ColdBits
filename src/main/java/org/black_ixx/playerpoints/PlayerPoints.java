// In package org.black_ixx.playerpoints
package org.black_ixx.playerpoints;

import dev.padrewin.coldbits.ColdBitsAPI;

public class PlayerPoints {

    private static PlayerPoints instance;

    private PlayerPoints() {
    }

    public static PlayerPoints getInstance() {
        if (instance == null) {
            instance = new PlayerPoints();
        }
        return instance;
    }

    public ColdBitsAPI getAPI() {
        return dev.padrewin.coldbits.ColdBits.getInstance().getAPI();
    }
}
