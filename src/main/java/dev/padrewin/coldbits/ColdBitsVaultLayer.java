package dev.padrewin.coldbits;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.models.Tuple;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.OfflinePlayer;

/**
 * Vault economy layer for ColdBits.
 */
public class ColdBitsVaultLayer implements Economy {

    private final ColdBits plugin;
    private final LocaleManager localeManager;

    public ColdBitsVaultLayer(ColdBits plugin) {
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public boolean isEnabled() {
        return this.plugin.isEnabled();
    }

    @Override
    public String getName() {
        return this.plugin.getName();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        return BitsUtils.formatBits((int) amount) + " " + (amount == 1 ? this.currencyNameSingular() : this.currencyNamePlural());
    }

    @Override
    public String currencyNamePlural() {
        return this.localeManager.getLocaleMessage("currency-plural");
    }

    @Override
    public String currencyNameSingular() {
        return this.localeManager.getLocaleMessage("currency-singular");
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return this.hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        UUID uuid = this.handleTranslation(playerName);
        return uuid != null ? this.plugin.getAPI().look(uuid) : 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return this.plugin.getAPI().look(player.getUniqueId());
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return this.getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return this.getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return this.getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return this.getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return this.has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return this.has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        int bits = (int) amount;
        UUID uuid = this.handleTranslation(playerName);
        if (uuid == null)
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Invalid player");

        boolean result = this.plugin.getAPI().take(uuid, bits);
        int balance = this.plugin.getAPI().look(uuid);

        EconomyResponse response;
        if (result) {
            response = new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
        } else {
            response = new EconomyResponse(amount, balance, ResponseType.FAILURE, "Insufficient balance");
        }

        return response;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        int bits = (int) amount;
        boolean result = this.plugin.getAPI().take(player.getUniqueId(), bits);
        int balance = this.plugin.getAPI().look(player.getUniqueId());

        EconomyResponse response;
        if (result) {
            response = new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
        } else {
            response = new EconomyResponse(amount, balance, ResponseType.FAILURE, "Insufficient balance");
        }

        return response;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return this.withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return this.withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        int bits = (int) amount;
        UUID uuid = this.handleTranslation(playerName);
        if (uuid == null)
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Invalid player");

        boolean result = this.plugin.getAPI().give(uuid, bits);
        int balance = this.plugin.getAPI().look(uuid);

        EconomyResponse response;
        if (result) {
            response = new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
        } else {
            response = new EconomyResponse(amount, balance, ResponseType.FAILURE, null);
        }

        return response;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        int bits = (int) amount;
        boolean result = this.plugin.getAPI().give(player.getUniqueId(), bits);
        int balance = this.plugin.getAPI().look(player.getUniqueId());

        EconomyResponse response;
        if (result) {
            response = new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
        } else {
            response = new EconomyResponse(amount, balance, ResponseType.FAILURE, null);
        }
        return response;
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return this.depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return this.depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "ColdBits does not support banks");
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return this.createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String world) {
        return true;
    }

    private UUID handleTranslation(String name) {
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            Tuple<UUID, String> tuple = BitsUtils.getPlayerByName(name);
            if (tuple != null) {
                return tuple.getFirst();
            } else {
                return null;
            }
        }
    }

}
