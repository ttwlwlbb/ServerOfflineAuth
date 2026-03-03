package cn.citprobe.ServerOfflineAuth;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public class StorageManager {
    private static IStorageProvider currentProvider;
    private static String currentStorageType;
    private static final Path MARKER_FILE = Paths.get("storage_type.txt");

    public static void init() {
        String configType = Config.SERVER.storageType.get();
        String migrateTarget = Config.SERVER.migrateTo.get();

        // 1. 初始化当前存储
        currentProvider = createProvider(configType);
        try {
            currentProvider.init();
            currentStorageType = configType;
            saveCurrentStorageType(configType);
            ServerOfflineAuth.LOGGER.info("存储系统初始化，使用类型: {}", configType);
        } catch (Exception e) {
            ServerOfflineAuth.LOGGER.error("初始化存储提供者失败，将使用 JSON 作为回退", e);
            currentProvider = new JsonStorageProvider();
            try {
                currentProvider.init();
            } catch (Exception ex) {
                ServerOfflineAuth.LOGGER.error("JSON 存储初始化也失败，无法继续", ex);
                throw new RuntimeException("无法初始化任何存储提供者", ex);
            }
            currentStorageType = "json";
            saveCurrentStorageType("json");
        }

        // 2. 检查是否需要迁移
        if (migrateTarget != null && !migrateTarget.isEmpty() && !migrateTarget.equalsIgnoreCase(currentStorageType)) {
            ServerOfflineAuth.LOGGER.info("检测到迁移请求：从 {} 迁移到 {}", currentStorageType, migrateTarget);
            boolean success = performMigration(currentStorageType, migrateTarget);

            if (success) {
                // 迁移成功，更新配置和当前存储
                try {
                    // 初始化目标存储作为新的 currentProvider
                    IStorageProvider newProvider = createProvider(migrateTarget);
                    newProvider.init();

                    // 替换当前 provider
                    currentProvider = newProvider;
                    currentStorageType = migrateTarget;
                    saveCurrentStorageType(migrateTarget);

                    // 修改配置文件中的 storageType 并清空 migrateTo
                    Config.SERVER.storageType.set(migrateTarget);
                    Config.SERVER.migrateTo.set("");
                    Config.SERVER.storageType.save();
                    Config.SERVER.migrateTo.save();

                    ServerOfflineAuth.LOGGER.info("数据迁移成功，存储类型已切换为 {}", migrateTarget);
                } catch (Exception e) {
                    ServerOfflineAuth.LOGGER.error("迁移后初始化新存储失败，将保持原有存储", e);
                    // 保持原 provider 不变，配置不改
                }
            } else {
                ServerOfflineAuth.LOGGER.error("数据迁移失败，请检查日志。配置未更改。");
            }
        }
    }

    private static IStorageProvider createProvider(String type) {
        if ("mysql".equalsIgnoreCase(type)) {
            return new MysqlStorageProvider();
        } else {
            return new JsonStorageProvider();
        }
    }

    private static boolean performMigration(String fromType, String toType) {
        IStorageProvider fromProvider = createProvider(fromType);
        IStorageProvider toProvider = createProvider(toType);
        try {
            fromProvider.init();
            toProvider.init();
            Map<UUID, PlayerData> allData = fromProvider.loadAll();
            ServerOfflineAuth.LOGGER.info("已从 {} 加载 {} 条数据", fromType, allData.size());
            toProvider.saveAll(allData);
            ServerOfflineAuth.LOGGER.info("数据已保存到 {}", toType);
            return true;
        } catch (Exception e) {
            ServerOfflineAuth.LOGGER.error("迁移过程中发生异常", e);
            return false;
        } finally {
            fromProvider.close();
            toProvider.close();
        }
    }

    private static void saveCurrentStorageType(String type) {
        try {
            Files.write(MARKER_FILE, type.getBytes());
        } catch (Exception e) {
            ServerOfflineAuth.LOGGER.warn("写入存储类型标记文件失败", e);
        }
    }

    public static PlayerData getPlayerData(UUID uuid) {
        return currentProvider.getPlayerData(uuid);
    }

    public static void putPlayerData(UUID uuid, PlayerData data) {
        currentProvider.putPlayerData(uuid, data);
    }

    public static boolean hasPlayerData(UUID uuid) {
        return currentProvider.hasPlayerData(uuid);
    }

    public static void close() {
        if (currentProvider != null) {
            currentProvider.close();
        }
    }
}