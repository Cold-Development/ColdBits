package dev.padrewin.coldbits.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.database.DataMigration;
import dev.padrewin.colddev.database.SQLiteConnector;
import dev.padrewin.colddev.manager.AbstractDataManager;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import dev.padrewin.coldbits.database.migrations._1_Create_Tables;
import dev.padrewin.coldbits.database.migrations._2_Add_Table_Username_Cache;
import dev.padrewin.coldbits.listeners.BitsMessageListener;
import dev.padrewin.coldbits.models.PendingTransaction;
import dev.padrewin.coldbits.models.SortedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class DataManager extends AbstractDataManager implements Listener {

    private LoadingCache<UUID, Integer> bitsCache;
    private final Map<UUID, Deque<PendingTransaction>> pendingTransactions;
    private final Map<UUID, String> pendingUsernameUpdates;

    public DataManager(ColdPlugin coldPlugin) {
        super(coldPlugin);

        this.pendingTransactions = new ConcurrentHashMap<>();
        this.pendingUsernameUpdates = new ConcurrentHashMap<>();

        Bukkit.getPluginManager().registerEvents(this, coldPlugin);
        coldPlugin.getScheduler().runTaskTimerAsync(this::update, 10L, 10L);
    }

    @Override
    public void reload() {
        super.reload();

        this.bitsCache = CacheBuilder.newBuilder()
                .concurrencyLevel(2)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .refreshAfterWrite(dev.padrewin.coldbits.setting.SettingKey.CACHE_DURATION.get(), TimeUnit.SECONDS)
                .build(new CacheLoader<UUID, Integer>() {
                    @Override
                    public Integer load(UUID uuid) throws Exception {
                        return DataManager.this.getBits(uuid);
                    }
                });
    }

    @Override
    public void disable() {
        this.update();

        this.bitsCache.invalidateAll();
        this.pendingTransactions.clear();
        this.pendingUsernameUpdates.clear();

        super.disable();
    }

    /**
     * Pushes any bits changes to the database and removes stale cache entries
     */
    private void update() {
        // Push any bits changes to the database
        Map<UUID, Integer> transactions = new HashMap<>();
        Map<UUID, Deque<PendingTransaction>> processingPendingTransactions;
        synchronized (this.pendingTransactions) {
            processingPendingTransactions = new HashMap<>(this.pendingTransactions);
            this.pendingTransactions.clear();
        }

        for (Map.Entry<UUID, Deque<PendingTransaction>> entry : processingPendingTransactions.entrySet()) {
            UUID uuid = entry.getKey();
            int bits = this.getEffectiveBits(uuid, entry.getValue());
            this.bitsCache.put(uuid, bits);
            transactions.put(uuid, bits);
        }

        if (!transactions.isEmpty())
            this.updateBits(transactions);

        if (!this.pendingUsernameUpdates.isEmpty()) {
            this.updateCachedUsernames(this.pendingUsernameUpdates);
            this.pendingUsernameUpdates.clear();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED)
            this.bitsCache.put(event.getUniqueId(), this.getBits(event.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.pendingUsernameUpdates.put(player.getUniqueId(), player.getName());
    }

    /**
     * Gets the effective amount of bits that a player has (includes pending transactions)
     *
     * @param playerId The player ID to use to get the bits
     * @return the effective bits value
     */
    public int getEffectiveBits(UUID playerId) {
        return this.getEffectiveBits(playerId, this.pendingTransactions.get(playerId));
    }

    private int getEffectiveBits(UUID playerId, Deque<PendingTransaction> transactions) {
        // Get the cached amount or fetch it fresh from the database
        int bits;
        try {
            bits = this.bitsCache.get(playerId);
        } catch (ExecutionException e) {
            e.printStackTrace();
            bits = 0;
        }

        // Apply any pending transactions
        if (transactions != null) {
            for (PendingTransaction transaction : transactions) {
                switch (transaction.getType()) {
                    case SET:
                        bits = transaction.getAmount();
                        break;

                    case OFFSET:
                        bits += transaction.getAmount();
                        break;
                }
            }
        }

        return bits;
    }

    /**
     * Refreshes a player's bits to the value in the database if they are online
     *
     * @param uuid The player's UUID
     */
    public void refreshBits(UUID uuid) {
        this.bitsCache.invalidate(uuid);
    }

    /**
     * Performs a database query and hangs the current thread, also caches the bits entry
     *
     * @param playerId The UUID of the Player
     * @return the amount of bits the Player has
     */
    private int getBits(UUID playerId) {
        AtomicInteger value = new AtomicInteger();
        AtomicBoolean generate = new AtomicBoolean(false);
        this.databaseConnector.connect(connection -> {
            String query = "SELECT bits FROM " + this.getBitsTableName() + " WHERE " + this.getUuidColumnName() + " = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, playerId.toString());
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    value.set(result.getInt(1));
                } else {
                    generate.set(true);
                }
            }
        });

        if (generate.get()) {
            int startingBalance = dev.padrewin.coldbits.setting.SettingKey.STARTING_BALANCE.get();
            this.setBits(playerId, startingBalance);
            value.set(startingBalance);
        }

        return value.get();
    }

    private Deque<PendingTransaction> getPendingTransactions(UUID playerId) {
        return this.pendingTransactions.computeIfAbsent(playerId, x -> new ConcurrentLinkedDeque<>());
    }

    /**
     * Adds a pending transaction to set the player's bits to a specified amount
     *
     * @param playerId The Player to set the bits of
     * @param amount The amount to set to
     * @return true if the transaction was successful, false otherwise
     */
    public boolean setBits(UUID playerId, int amount) {
        if (amount < 0)
            return false;

        this.getPendingTransactions(playerId).add(new PendingTransaction(PendingTransaction.TransactionType.SET, amount));
        return true;
    }

    /**
     * Adds a pending transaction to offset the player's bits by a specified amount
     *
     * @param playerId The Player to offset the bits of
     * @param amount The amount to offset by
     * @return true if the transaction was successful, false otherwise
     */
    public boolean offsetBits(UUID playerId, int amount) {
        int bits = this.getEffectiveBits(playerId);
        if (bits + amount < 0)
            return false;

        this.getPendingTransactions(playerId).add(new PendingTransaction(PendingTransaction.TransactionType.OFFSET, amount));
        return true;
    }

    private void updateBits(Map<UUID, Integer> transactions) {
        this.databaseConnector.connect(connection -> {
            String query = "REPLACE INTO " + this.getBitsTableName() + " (" + this.getUuidColumnName() + ", bits) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (Map.Entry<UUID, Integer> entry : transactions.entrySet()) {
                    statement.setString(1, entry.getKey().toString());
                    statement.setInt(2, Math.max(0, entry.getValue()));
                    statement.addBatch();

                    // Update cached value
                    this.bitsCache.put(entry.getKey(), entry.getValue());

                    // Send update to BungeeCord if enabled
                    if (dev.padrewin.coldbits.setting.SettingKey.BUNGEECORD_SEND_UPDATES.get() && this.coldPlugin.isEnabled()) {
                        ByteArrayDataOutput output = ByteStreams.newDataOutput();
                        output.writeUTF("Forward");
                        output.writeUTF("ONLINE");
                        output.writeUTF(BitsMessageListener.REFRESH_SUBCHANNEL);

                        byte[] bytes = entry.getKey().toString().getBytes(StandardCharsets.UTF_8);
                        output.writeShort(bytes.length);
                        output.write(bytes);

                        Player attachedPlayer = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                        if (attachedPlayer != null)
                            attachedPlayer.sendPluginMessage(this.coldPlugin, BitsMessageListener.CHANNEL, output.toByteArray());
                    }
                }
                statement.executeBatch();
            }
        });
    }

    public boolean offsetAllBits(int amount) {
        if (amount == 0)
            return true;

        this.databaseConnector.connect(connection -> {
            String function = this.databaseConnector instanceof SQLiteConnector ? "MAX" : "GREATEST";
            String query = "UPDATE " + this.getBitsTableName() + " SET bits = " + function + "(0, bits + ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, amount);
                statement.executeUpdate();
            }
        });

        for (Player player : Bukkit.getOnlinePlayers())
            this.offsetBits(player.getUniqueId(), amount);

        return true;
    }

    public boolean doesDataExist() {
        AtomicInteger count = new AtomicInteger();
        this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + this.getBitsTableName());
                result.next();
                count.set(result.getInt(1));
            }
        });
        return count.get() > 0;
    }

    public List<SortedPlayer> getTopSortedBits(Integer limit) {
        List<SortedPlayer> players = new ArrayList<>();
        this.databaseConnector.connect(connection -> {
            String query = "SELECT t." + this.getUuidColumnName() + ", username, bits FROM " + this.getBitsTableName() + " t " +
                    "LEFT JOIN " + this.getTablePrefix() + "username_cache c ON t.uuid = c.uuid " +
                    "ORDER BY bits DESC" + (limit != null ? " LIMIT " + limit : "");
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString(1));
                    String username = result.getString(2);
                    Integer bitsValue = this.bitsCache.getIfPresent(uuid);
                    if (bitsValue == null)
                        bitsValue = result.getInt(3);

                    if (username != null) {
                        players.add(new SortedPlayer(uuid, username, bitsValue));
                    } else {
                        players.add(new SortedPlayer(uuid, bitsValue));
                    }
                }
            }
        });
        return players;
    }

    public Map<UUID, Long> getOnlineTopSortedPointPositions() {
        Map<UUID, Long> players = new HashMap<>();
        if (Bukkit.getOnlinePlayers().isEmpty())
            return players;

        String uuidList = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).map(x -> "'" + x + "'").collect(Collectors.joining(", "));
        this.databaseConnector.connect(connection -> {
            String tableName = this.getBitsTableName();
            String query = "SELECT t." + this.getUuidColumnName() + ", (SELECT COUNT(*) FROM " + tableName + " x WHERE x.bits >= t.bits) AS position " +
                    "FROM " + tableName + " t " +
                    "WHERE t.uuid IN (" + uuidList + ")";
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString(1));
                    players.put(uuid, result.getLong(2));
                }
            }
        });
        return players;
    }

    public void importData(SortedSet<SortedPlayer> data, Map<UUID, String> cachedUsernames) {
        this.bitsCache.invalidateAll();
        this.pendingTransactions.clear();

        this.databaseConnector.connect(connection -> {
            String purgeQuery = "DELETE FROM " + this.getBitsTableName();
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(purgeQuery);
            }

            String batchInsert = "INSERT INTO " + this.getBitsTableName() + " (" + this.getUuidColumnName() + ", bits) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                for (SortedPlayer playerData : data) {
                    statement.setString(1, playerData.getUniqueId().toString());
                    statement.setInt(2, playerData.getBits());
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            if (!cachedUsernames.isEmpty())
                this.updateCachedUsernames(cachedUsernames);
        });
    }

    public boolean importLegacyTable(String tableName) {
        this.bitsCache.invalidateAll();
        this.pendingTransactions.clear();

        AtomicBoolean value = new AtomicBoolean();
        this.databaseConnector.connect(connection -> {
            try {
                String selectQuery = "SELECT playername, bits FROM " + tableName;
                Map<UUID, Integer> bits = new HashMap<>();
                try (Statement statement = connection.createStatement()) {
                    ResultSet result = statement.executeQuery(selectQuery);
                    while (result.next()) {
                        UUID uuid = UUID.fromString(result.getString(1));
                        int bitValue = result.getInt(2);
                        bits.put(uuid, bitValue);
                    }
                }

                boolean isSqlite = this.databaseConnector instanceof SQLiteConnector;
                String insertQuery;
                if (isSqlite) {
                    insertQuery = "REPLACE INTO " + this.getBitsTableName() + " (" + this.getUuidColumnName() + ", bits) VALUES (?, ?)";
                } else {
                    insertQuery = "INSERT INTO " + this.getBitsTableName() + " (" + this.getUuidColumnName() + ", bits) VALUES (?, ?) ON DUPLICATE KEY UPDATE bits = ?";
                }

                try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                    for (Map.Entry<UUID, Integer> entry : bits.entrySet()) {
                        statement.setString(1, entry.getKey().toString());
                        statement.setInt(2, entry.getValue());
                        if (!isSqlite)
                            statement.setInt(3, entry.getValue());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }

                value.set(true);
            } catch (Exception e) {
                value.set(false);
                e.printStackTrace();
            }
        });
        return value.get();
    }

    public void updateCachedUsernames(Map<UUID, String> cachedUsernames) {
        this.databaseConnector.connect(connection -> {
            String query;
            boolean isSqlite = this.databaseConnector instanceof SQLiteConnector;
            if (isSqlite) {
                query = "REPLACE INTO " + this.getTablePrefix() + "username_cache (uuid, username) VALUES (?, ?)";
            } else {
                query = "INSERT INTO " + this.getTablePrefix() + "username_cache (uuid, username) VALUES (?, ?) ON DUPLICATE KEY UPDATE username = ?";
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (Map.Entry<UUID, String> entry : cachedUsernames.entrySet()) {
                    statement.setString(1, entry.getKey().toString());
                    statement.setString(2, entry.getValue());
                    if (!isSqlite)
                        statement.setString(3, entry.getValue());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
    }

    public String lookupCachedUsername(UUID uuid) {
        AtomicReference<String> value = new AtomicReference<>();
        this.databaseConnector.connect(connection -> {
            String query = "SELECT username FROM " + this.getTablePrefix() + "username_cache WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                ResultSet result = statement.executeQuery();
                if (result.next())
                    value.set(result.getString(1));
            }
        });

        String name = value.get();
        if (name == null) {
            return "Unknown";
        } else {
            return name;
        }
    }

    public UUID lookupCachedUUID(String username) {
        AtomicReference<UUID> value = new AtomicReference<>();
        this.databaseConnector.connect(connection -> {
            String query = "SELECT uuid FROM " + this.getTablePrefix() + "username_cache WHERE LOWER(username) = LOWER(?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                ResultSet result = statement.executeQuery();
                if (result.next())
                    value.set(UUID.fromString(result.getString(1)));
            }
        });

        return value.get();
    }

    private String getBitsTableName() {
        if (dev.padrewin.coldbits.setting.SettingKey.LEGACY_DATABASE_MODE.get()) {
            return dev.padrewin.coldbits.setting.SettingKey.LEGACY_DATABASE_NAME.get();
        } else {
            return super.getTablePrefix() + "bits";
        }
    }

    private String getUuidColumnName() {
        if (dev.padrewin.coldbits.setting.SettingKey.LEGACY_DATABASE_MODE.get()) {
            return "playername";
        } else {
            return "uuid";
        }
    }

    @Override
    public List<Supplier<? extends DataMigration>> getDataMigrations() {
        return Arrays.asList(
                _1_Create_Tables::new,
                _2_Add_Table_Username_Cache::new
        );
    }
}
