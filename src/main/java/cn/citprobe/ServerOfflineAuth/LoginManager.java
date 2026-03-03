package cn.citprobe.ServerOfflineAuth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoginManager {
    private static final Map<UUID, Boolean> loginStatus = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> joinTime = new ConcurrentHashMap<>();
    private static final Map<UUID, GameModeSnapshot> originalPlayerState = new ConcurrentHashMap<>();

    public static boolean isAuthenticated(ServerPlayer player) {
        return loginStatus.getOrDefault(player.getUUID(), false);
    }

    public static void setAuthenticated(ServerPlayer player, boolean authenticated) {
        loginStatus.put(player.getUUID(), authenticated);
    }

    public static void removePlayer(ServerPlayer player) {
        loginStatus.remove(player.getUUID());
        joinTime.remove(player.getUUID());
        originalPlayerState.remove(player.getUUID());
    }

    public static void recordJoinTime(ServerPlayer player) {
        joinTime.put(player.getUUID(), System.currentTimeMillis());
    }

    public static long getJoinTime(ServerPlayer player) {
        return joinTime.getOrDefault(player.getUUID(), 0L);
    }

    // 备份玩家状态（登录前）
    public static void backupPlayerState(ServerPlayer player) {
        originalPlayerState.put(player.getUUID(), new GameModeSnapshot(
                player.gameMode.getGameModeForPlayer(),
                player.getAbilities().mayfly,
                player.getAbilities().flying
        ));
    }

    // 恢复玩家状态（登录后）
    public static void restorePlayerState(ServerPlayer player) {
        GameModeSnapshot snapshot = originalPlayerState.remove(player.getUUID());
        if (snapshot != null) {
            player.setGameMode(snapshot.gameMode);
            player.getAbilities().mayfly = snapshot.mayFly;
            player.getAbilities().flying = snapshot.flying;
            player.onUpdateAbilities();
        }
    }

    private static class GameModeSnapshot {
        final GameType gameMode;
        final boolean mayFly;
        final boolean flying;
        GameModeSnapshot(GameType gameMode, boolean mayFly, boolean flying) {
            this.gameMode = gameMode;
            this.mayFly = mayFly;
            this.flying = flying;
        }
    }
}