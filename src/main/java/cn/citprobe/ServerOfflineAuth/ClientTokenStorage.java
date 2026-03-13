package cn.citprobe.ServerOfflineAuth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@OnlyIn(Dist.CLIENT)
public class ClientTokenStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path TOKEN_FILE = Paths.get("config", "server_offline_auth_token.json");

    public static class TokenData {
        public String token;
        public long expiry;
    }

    public static void saveToken(String token, long expiry) {
        TokenData data = new TokenData();
        data.token = token;
        data.expiry = expiry;
        try (Writer writer = new FileWriter(TOKEN_FILE.toFile())) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            ServerOfflineAuth.LOGGER.error("保存 token 失败", e);
        }
    }

    public static TokenData loadToken() {
        File file = TOKEN_FILE.toFile();
        if (!file.exists()) return null;
        try (Reader reader = new FileReader(file)) {
            return GSON.fromJson(reader, TokenData.class);
        } catch (IOException e) {
            ServerOfflineAuth.LOGGER.error("加载 token 失败", e);
            return null;
        }
    }

    public static void clearToken() {
        saveToken("", 0);
    }
}