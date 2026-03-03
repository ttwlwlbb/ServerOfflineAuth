package cn.citprobe.ServerOfflineAuth;

import java.util.Map;
import java.util.UUID;

public interface IStorageProvider {
    void init() throws Exception;
    PlayerData getPlayerData(UUID uuid);
    void putPlayerData(UUID uuid, PlayerData data);
    boolean hasPlayerData(UUID uuid);
    Map<UUID, PlayerData> loadAll(); // 用于迁移
    void saveAll(Map<UUID, PlayerData> allData); // 用于迁移
    void close();
}