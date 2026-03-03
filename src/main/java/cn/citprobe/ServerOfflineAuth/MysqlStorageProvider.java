package cn.citprobe.ServerOfflineAuth;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MysqlStorageProvider implements IStorageProvider {
    private Connection connection;
    private String tableName;
    private Map<UUID, PlayerData> cache = new ConcurrentHashMap<>(); // 可选的缓存，简化实现
    private boolean cacheLoaded = false;

    @Override
    public void init() throws Exception {
        String host = Config.SERVER.mysqlHost.get();
        int port = Config.SERVER.mysqlPort.get();
        String database = Config.SERVER.mysqlDatabase.get();
        String user = Config.SERVER.mysqlUsername.get();
        String password = Config.SERVER.mysqlPassword.get();
        tableName = Config.SERVER.mysqlTable.get();

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(url, user, password);

        createTableIfNotExists();
        ServerOfflineAuth.LOGGER.info("MySQL 存储初始化成功，表: {}", tableName);
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "uuid CHAR(36) PRIMARY KEY, " +
                "hashed_password TEXT NOT NULL, " +
                "registered_time BIGINT NOT NULL, " +
                "last_ip VARCHAR(45)" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void ensureCacheLoaded() {
        if (!cacheLoaded) {
            cache.putAll(loadAll());
            cacheLoaded = true;
        }
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        ensureCacheLoaded();
        return cache.get(uuid);
    }

    @Override
    public void putPlayerData(UUID uuid, PlayerData data) {
        ensureCacheLoaded();
        cache.put(uuid, data);
        // 直接写入数据库
        String upsertSql = "INSERT INTO " + tableName + " (uuid, hashed_password, registered_time, last_ip) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE hashed_password=?, registered_time=?, last_ip=?";
        try (PreparedStatement ps = connection.prepareStatement(upsertSql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, data.getHashedPassword());
            ps.setLong(3, data.getRegisteredTime());
            ps.setString(4, data.getLastIp());
            ps.setString(5, data.getHashedPassword());
            ps.setLong(6, data.getRegisteredTime());
            ps.setString(7, data.getLastIp());
            ps.executeUpdate();
        } catch (SQLException e) {
            ServerOfflineAuth.LOGGER.error("保存玩家数据到 MySQL 失败", e);
        }
    }

    @Override
    public boolean hasPlayerData(UUID uuid) {
        ensureCacheLoaded();
        return cache.containsKey(uuid);
    }

    @Override
    public Map<UUID, PlayerData> loadAll() {
        Map<UUID, PlayerData> map = new HashMap<>();
        String sql = "SELECT uuid, hashed_password, registered_time, last_ip FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String hashedPassword = rs.getString("hashed_password");
                long registeredTime = rs.getLong("registered_time");
                String lastIp = rs.getString("last_ip");
                PlayerData data = new PlayerData(uuid, hashedPassword, registeredTime, lastIp);
                map.put(uuid, data);
            }
        } catch (SQLException e) {
            ServerOfflineAuth.LOGGER.error("从 MySQL 加载所有玩家数据失败", e);
        }
        return map;
    }

    @Override
    public void saveAll(Map<UUID, PlayerData> allData) {
        cache.putAll(allData);
        // 简单实现：先清空表，再批量插入（生产环境可优化）
        String deleteSql = "DELETE FROM " + tableName;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(deleteSql);
        } catch (SQLException e) {
            ServerOfflineAuth.LOGGER.error("清空 MySQL 表失败", e);
            return;
        }

        String insertSql = "INSERT INTO " + tableName + " (uuid, hashed_password, registered_time, last_ip) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            for (Map.Entry<UUID, PlayerData> entry : allData.entrySet()) {
                ps.setString(1, entry.getKey().toString());
                ps.setString(2, entry.getValue().getHashedPassword());
                ps.setLong(3, entry.getValue().getRegisteredTime());
                ps.setString(4, entry.getValue().getLastIp());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            ServerOfflineAuth.LOGGER.error("批量插入 MySQL 数据失败", e);
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                ServerOfflineAuth.LOGGER.error("关闭 MySQL 连接失败", e);
            }
        }
    }
}