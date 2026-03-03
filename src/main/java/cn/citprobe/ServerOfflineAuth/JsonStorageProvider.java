package cn.citprobe.ServerOfflineAuth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JsonStorageProvider implements IStorageProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private Map<UUID, PlayerData> playersData = new ConcurrentHashMap<>();
    private boolean loaded = false;
    private Path dataFile;

    @Override
    public void init() {
        dataFile = FMLPaths.GAMEDIR.get().resolve(Config.SERVER.dataFileName.get());
        // 不立即加载，延迟到需要时
        ServerOfflineAuth.LOGGER.info("JSON 存储初始化，文件: {}", dataFile.toAbsolutePath());
    }

    private void ensureLoaded() {
        if (!loaded) {
            load();
            loaded = true;
        }
    }

    private Path getDataFile() {
        return dataFile; // 已在 init 中确定，无需实时获取
    }

    private void load() {
        File file = getDataFile().toFile();
        if (!file.exists()) {
            playersData = new ConcurrentHashMap<>();
            ServerOfflineAuth.LOGGER.info("JSON 数据文件不存在，将创建新文件: {}", file.getAbsolutePath());
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>() {}.getType();
            Map<UUID, PlayerData> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                playersData = new ConcurrentHashMap<>(loaded);
                ServerOfflineAuth.LOGGER.info("已从 JSON 加载 {} 个玩家数据", playersData.size());
            } else {
                playersData = new ConcurrentHashMap<>();
                ServerOfflineAuth.LOGGER.warn("JSON 数据文件为空，将初始化新数据");
            }
        } catch (IOException e) {
            ServerOfflineAuth.LOGGER.error("加载 JSON 玩家数据失败", e);
            playersData = new ConcurrentHashMap<>();
        }
    }

    private void save() {
        if (!loaded && playersData.isEmpty()) return;
        File file = getDataFile().toFile();
        file.getParentFile().mkdirs();

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(playersData, writer);
            ServerOfflineAuth.LOGGER.debug("JSON 数据保存成功，共 {} 条记录", playersData.size());
        } catch (IOException e) {
            ServerOfflineAuth.LOGGER.error("保存 JSON 玩家数据失败", e);
        }
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        ensureLoaded();
        return playersData.get(uuid);
    }

    @Override
    public void putPlayerData(UUID uuid, PlayerData data) {
        ensureLoaded();
        playersData.put(uuid, data);
        save();
    }

    @Override
    public boolean hasPlayerData(UUID uuid) {
        ensureLoaded();
        return playersData.containsKey(uuid);
    }

    @Override
    public Map<UUID, PlayerData> loadAll() {
        ensureLoaded();
        return new ConcurrentHashMap<>(playersData);
    }

    @Override
    public void saveAll(Map<UUID, PlayerData> allData) {
        playersData = new ConcurrentHashMap<>(allData);
        save();
    }

    @Override
    public void close() {
        // 无需特殊关闭
    }
}