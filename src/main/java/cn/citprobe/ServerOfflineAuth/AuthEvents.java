package cn.citprobe.ServerOfflineAuth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ServerOfflineAuth.MODID)
public class AuthEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LoginManager.recordJoinTime(player);

            // 备份玩家原始游戏模式，然后强制设为冒险模式
            LoginManager.backupPlayerState(player);
            player.setGameMode(GameType.ADVENTURE);

            if (Config.SERVER.joinMessageEnabled.get()) {
                if (!StorageManager.hasPlayerData(player.getUUID())) {
                    player.sendSystemMessage(Component.literal("请使用 /register <密码> <确认密码> 注册").withStyle(ChatFormatting.RED));
                } else {
                    player.sendSystemMessage(Component.literal("请使用 /login <密码> 登录").withStyle(ChatFormatting.RED));
                }
            }
            LoginManager.setAuthenticated(player, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (LoginManager.isAuthenticated(player)) {
                // 保存玩家最终状态到数据库
                PlayerData data = StorageManager.getPlayerData(player.getUUID());
                if (data != null) {
                    // 保存位置（原有代码）
                    data.setLastDimension(player.level().dimension().location().toString());
                    data.setLastX(player.getX());
                    data.setLastY(player.getY());
                    data.setLastZ(player.getZ());
                    data.setLastYRot(player.getYRot());
                    data.setLastXRot(player.getXRot());
                    // 保存游戏模式
                    data.setGameMode(player.gameMode.getGameModeForPlayer().getId());
                    data.setMayFly(player.getAbilities().mayfly);
                    data.setFlying(player.getAbilities().flying);
                    StorageManager.putPlayerData(player.getUUID(), data);
                }
            }
            LoginManager.removePlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.isAuthenticated(player)) {
                // 强制冒险模式
                player.setGameMode(GameType.ADVENTURE);

                // 解决浮空问题：如果玩家在空中且无飞行权限，临时允许飞行
                if (!player.onGround() && !player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }

                // 拉回出生点 - 添加空检查
                if (player.getServer() != null) {
                    var overworld = player.getServer().overworld();
                    if (overworld != null) {
                        var spawnPos = overworld.getSharedSpawnPos();
                        player.teleportTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                    } else {
                        ServerOfflineAuth.LOGGER.warn("无法获取主世界，无法传送玩家 {}", player.getName().getString());
                    }
                } else {
                    ServerOfflineAuth.LOGGER.warn("服务器实例为 null，无法传送玩家 {}", player.getName().getString());
                }

                player.setDeltaMovement(0, 0, 0);
                player.hurtMarked = true;
            }
        }
    }

    // 阻止所有玩家交互事件
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getEntity() instanceof ServerPlayer player &&
                !LoginManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // 阻止放置方块
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !LoginManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // 阻止破坏方块
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player &&
                !LoginManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // 阻止使用物品（如吃东西、投掷）
    @SubscribeEvent
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player &&
                !LoginManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // 阻止右键点击实体
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player &&
                !LoginManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // 阻止攻击实体
    @SubscribeEvent
    public static void onPlayerAttack(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player &&
                !LoginManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // 阻止聊天
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        if (!LoginManager.isAuthenticated(event.getPlayer())) {
            event.setCanceled(true);
            event.getPlayer().sendSystemMessage(Component.literal("请先登录！").withStyle(ChatFormatting.RED));
        }
    }

    // 超时踢出
    @SubscribeEvent
    public static void onTimeoutCheck(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.isAuthenticated(player)) {
                int timeout = Config.SERVER.loginTimeout.get();
                if (timeout > 0) {
                    long joinTime = LoginManager.getJoinTime(player);
                    if (System.currentTimeMillis() - joinTime > timeout * 1000L) {
                        player.connection.disconnect(Component.literal("登录超时，请重新加入"));
                    }
                }
            }
        }
    }
}