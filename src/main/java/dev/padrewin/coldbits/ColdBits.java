package dev.padrewin.coldbits;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.config.ColdSetting;
import dev.padrewin.colddev.database.DatabaseConnector;
import dev.padrewin.colddev.database.MySQLConnector;
import dev.padrewin.colddev.database.SQLiteConnector;
import dev.padrewin.colddev.manager.Manager;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import dev.padrewin.coldbits.hook.BitsPlaceholderExpansion;
import dev.padrewin.coldbits.listeners.BitsMessageListener;
import dev.padrewin.coldbits.listeners.VotifierListener;
import dev.padrewin.coldbits.manager.CommandManager;
import dev.padrewin.coldbits.manager.DataManager;
import dev.padrewin.coldbits.manager.LeaderboardManager;
import dev.padrewin.coldbits.manager.LocaleManager;
import dev.padrewin.coldbits.setting.SettingKey;
import dev.padrewin.coldbits.util.BitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

/**
 * Main plugin class for ColdBits.
 */
public class ColdBits extends ColdPlugin {

    private static ColdBits instance;
    private ColdBitsAPI api;
    private ColdBitsVaultLayer vaultLayer;

    String ANSI_RESET = "\u001B[0m";
    String ANSI_CHINESE_PURPLE = "\u001B[38;5;93m";
    String ANSI_PURPLE = "\u001B[35m";
    String ANSI_GREEN = "\u001B[32m";
    String ANSI_RED = "\u001B[31m";
    String ANSI_AQUA = "\u001B[36m";
    String ANSI_PINK = "\u001B[35m";
    String ANSI_YELLOW = "\u001B[33m";

    public ColdBits() {
        super("Cold-Development", "ColdBits", 23639, DataManager.class, LocaleManager.class, null);
        instance = this;
        this.api = new ColdBitsAPI(this);
    }

    @Override
    public void enable() {

        instance = this;
        this.api = new ColdBitsAPI(this);

        String name = getDescription().getName();
        getLogger().info("");
        getLogger().info(ANSI_CHINESE_PURPLE + "  ____ ___  _     ____  " + ANSI_RESET);
        getLogger().info(ANSI_PINK + " / ___/ _ \\| |   |  _ \\ " + ANSI_RESET);
        getLogger().info(ANSI_CHINESE_PURPLE + "| |  | | | | |   | | | |" + ANSI_RESET);
        getLogger().info(ANSI_PINK + "| |__| |_| | |___| |_| |" + ANSI_RESET);
        getLogger().info(ANSI_CHINESE_PURPLE + " \\____\\___/|_____|____/ " + ANSI_RESET);
        getLogger().info("    " + ANSI_GREEN + name + ANSI_RED + " v" + getDescription().getVersion() + ANSI_RESET);
        getLogger().info(ANSI_PURPLE + "    Author(s): " + ANSI_PURPLE + getDescription().getAuthors().get(0) + ANSI_RESET);
        getLogger().info(ANSI_AQUA + "    (c) Cold Development ❄" + ANSI_RESET);
        getLogger().info("");

        // Retrieve the type of database from the configuration
        String databaseType = getConfig().getString("database.type", "sqlite");

        DatabaseConnector connector;

        if (databaseType.equalsIgnoreCase("mysql")) {
            String hostname = getConfig().getString("database.mysql.hostname");
            int port = getConfig().getInt("database.mysql.port");
            String database = getConfig().getString("database.mysql.database");
            String username = getConfig().getString("database.mysql.username");
            String password = getConfig().getString("database.mysql.password");
            boolean useSSL = getConfig().getBoolean("database.mysql.useSSL", false);
            int poolSize = getConfig().getInt("database.mysql.poolSize", 10);

            connector = new MySQLConnector(this, hostname, port, database, username, password, useSSL, poolSize);
        } else {
            connector = new SQLiteConnector(this);
        }

        // Log the database URL/path
        String databasePath = connector.getDatabasePath();
        getLogger().info(ANSI_GREEN + "Database path: " + ANSI_YELLOW + databasePath + ANSI_RESET);
        getLogger().info("");

        if (SettingKey.VAULT.get() && Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.vaultLayer = new ColdBitsVaultLayer(this);

            // Check valid values for the priorities
            ServicePriority priority = null;
            String desiredPriority = SettingKey.VAULT_PRIORITY.get();
            for (ServicePriority value : ServicePriority.values()) {
                if (value.name().equalsIgnoreCase(desiredPriority)) {
                    priority = value;
                    break;
                }
            }

            if (priority == null) {
                this.getLogger().warning("vault-priority value in the config.yml is invalid, defaulting to Low.");
                priority = ServicePriority.Low;
            }

            Bukkit.getServicesManager().register(Economy.class, this.vaultLayer, this, priority);
        }

        if (SettingKey.BUNGEECORD_SEND_UPDATES.get()) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, BitsMessageListener.CHANNEL);
            Bukkit.getMessenger().registerIncomingPluginChannel(this, BitsMessageListener.CHANNEL, new BitsMessageListener(this));
        }

        this.getScheduler().runTask(() -> {
            // Register placeholders, if applicable
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                new BitsPlaceholderExpansion(this).register();

            // Register votifier listener, if applicable
            if (SettingKey.VOTE_ENABLED.get()) {
                Plugin votifier = Bukkit.getPluginManager().getPlugin("Votifier");
                if (votifier != null) {
                    Bukkit.getPluginManager().registerEvents(new VotifierListener(this), this);
                } else {
                    this.getLogger().warning("The hook for Votifier was enabled, but it does not appear to be installed.");
                }
            }
        });
    }

    @Override
    public void disable() {

        getLogger().info("");
        getLogger().info(ANSI_CHINESE_PURPLE + "ColdBits disabled." + ANSI_RESET);
        getLogger().info("");

        if (this.vaultLayer != null)
            Bukkit.getServicesManager().unregister(Economy.class, this.vaultLayer);

        if (SettingKey.BUNGEECORD_SEND_UPDATES.get()) {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(this);
            Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
        }
    }

    @Override
    public void reload() {
        super.reload();
        BitsUtils.setCachedValues(this);
    }

    @Override
    protected @NotNull List<Class<? extends Manager>> getManagerLoadPriority() {
        return Arrays.asList(
                CommandManager.class,
                LeaderboardManager.class
        );
    }

    @Override
    protected @NotNull List<ColdSetting<?>> getColdConfigSettings() {
        return SettingKey.getKeys();
    }

    @Override
    protected String[] getColdConfigHeader() {
        return new String[] {
                "  ____  ___   _      ____   ",
                " / ___|/ _ \\ | |    |  _ \\  ",
                "| |   | | | || |    | | | | ",
                "| |___| |_| || |___ | |_| | ",
                " \\____|\\___/ |_____|_____/  ",
                "                           "
        };
    }

    public static ColdBits getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ColdBits instance is not initialized!");
        }
        return instance;
    }

    /**
     * Get the plugin's API.
     *
     * @return API instance.
     */
    public ColdBitsAPI getAPI() {
        return api;
    }

}
