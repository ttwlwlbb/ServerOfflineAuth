package cn.citprobe.ServerOfflineAuth;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthCommands {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerRegisterCommand(dispatcher);
        registerLoginCommand(dispatcher);
        registerLogoutCommand(dispatcher);
        registerChangePasswordCommand(dispatcher);
        registerAdminCommand(dispatcher);
    }

    private static void registerRegisterCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("register")
                .then(Commands.argument("password", StringArgumentType.word())
                        .then(Commands.argument("confirm", StringArgumentType.word())
                                .executes(context -> {
                                    try {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        String pwd = StringArgumentType.getString(context, "password");
                                        String confirm = StringArgumentType.getString(context, "confirm");

                                        if (!pwd.equals(confirm)) {
                                            context.getSource().sendFailure(Component.literal("两次输入的密码不一致"));
                                            return 0;
                                        }

                                        LOGGER.info("玩家 {} 尝试注册，密码一致", player.getName().getString());

                                        if (StorageManager.hasPlayerData(player.getUUID())) {
                                            context.getSource().sendFailure(Component.literal("你已经注册过了，请使用 /login"));
                                            return 0;
                                        }

                                        LOGGER.info("玩家 {} 未注册，开始创建账户", player.getName().getString());

                                        String hashed = PasswordUtils.hashPassword(pwd);
                                        PlayerData data = new PlayerData(player.getUUID(), hashed, System.currentTimeMillis(), player.getIpAddress());
                                        data.setPlayerName(player.getName().getString());
                                        StorageManager.putPlayerData(player.getUUID(), data);
                                        LoginManager.setAuthenticated(player, true);
                                        LoginManager.restorePlayerState(player);
                                        teleportToLastLocation(player, data);

                                        context.getSource().sendSuccess(() -> Component.literal("注册成功，已自动登录"), true);
                                        return 1;
                                    } catch (Exception e) {
                                        LOGGER.error("注册命令执行过程中发生异常", e);
                                        context.getSource().sendFailure(Component.literal("注册失败，请查看服务器日志"));
                                        return 0;
                                    }
                                }))));
    }

    private static void registerLoginCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("login")
                .then(Commands.argument("password", StringArgumentType.word())
                        .executes(context -> {
                            try {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                String pwd = StringArgumentType.getString(context, "password");

                                PlayerData data = StorageManager.getPlayerData(player.getUUID());
                                if (data == null) {
                                    context.getSource().sendFailure(Component.literal("你还没有注册，请先使用 /register"));
                                    return 0;
                                }

                                if (LoginManager.isAuthenticated(player)) {
                                    context.getSource().sendFailure(Component.literal("你已经登录了"));
                                    return 0;
                                }

                                if (PasswordUtils.checkPassword(pwd, data.getHashedPassword())) {
                                    // 更新玩家名称
                                    data.setPlayerName(player.getName().getString());
                                    StorageManager.putPlayerData(player.getUUID(), data);

                                    LoginManager.setAuthenticated(player, true);
                                    LoginManager.restorePlayerState(player);
                                    teleportToLastLocation(player, data);
                                    context.getSource().sendSuccess(() -> Component.literal("登录成功"), true);
                                    return 1;
                                } else {
                                    context.getSource().sendFailure(Component.literal("密码错误"));
                                    return 0;
                                }
                            } catch (Exception e) {
                                LOGGER.error("登录命令执行过程中发生异常", e);
                                context.getSource().sendFailure(Component.literal("登录失败，请查看服务器日志"));
                                return 0;
                            }
                        })));
    }

    private static void registerLogoutCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("logout")
                .executes(context -> {
                    try {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        if (!LoginManager.isAuthenticated(player)) {
                            context.getSource().sendFailure(Component.literal("你还未登录"));
                            return 0;
                        }
                        LoginManager.setAuthenticated(player, false);
                        // 登出时备份状态并设为冒险模式
                        LoginManager.backupPlayerState(player);
                        player.setGameMode(GameType.ADVENTURE);
                        context.getSource().sendSuccess(() -> Component.literal("已登出"), true);
                        return 1;
                    } catch (Exception e) {
                        LOGGER.error("登出命令执行过程中发生异常", e);
                        context.getSource().sendFailure(Component.literal("登出失败，请查看服务器日志"));
                        return 0;
                    }
                }));
    }

    private static void registerChangePasswordCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("changepassword")
                .then(Commands.argument("old", StringArgumentType.word())
                        .then(Commands.argument("new", StringArgumentType.word())
                                .executes(context -> {
                                    try {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        String oldPwd = StringArgumentType.getString(context, "old");
                                        String newPwd = StringArgumentType.getString(context, "new");

                                        PlayerData data = StorageManager.getPlayerData(player.getUUID());
                                        if (data == null) {
                                            context.getSource().sendFailure(Component.literal("你还没有注册"));
                                            return 0;
                                        }

                                        if (!PasswordUtils.checkPassword(oldPwd, data.getHashedPassword())) {
                                            context.getSource().sendFailure(Component.literal("旧密码错误"));
                                            return 0;
                                        }

                                        String newHashed = PasswordUtils.hashPassword(newPwd);
                                        data.setHashedPassword(newHashed);
                                        StorageManager.putPlayerData(player.getUUID(), data);
                                        context.getSource().sendSuccess(() -> Component.literal("密码已修改"), true);
                                        return 1;
                                    } catch (Exception e) {
                                        LOGGER.error("修改密码命令执行过程中发生异常", e);
                                        context.getSource().sendFailure(Component.literal("修改密码失败，请查看服务器日志"));
                                        return 0;
                                    }
                                }))));
    }

    private static void registerAdminCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("serverofflineauth")
                .then(Commands.literal("admin")
                        .requires(source -> source.hasPermission(2)) // 需要 OP 权限
                        .then(Commands.literal("login")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> {
                                            try {
                                                ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                                if (target == null) {
                                                    context.getSource().sendFailure(Component.literal("目标玩家不在线"));
                                                    return 0;
                                                }
                                                if (LoginManager.isAuthenticated(target)) {
                                                    context.getSource().sendFailure(Component.literal("目标玩家已经登录"));
                                                    return 0;
                                                }
                                                // 强制登录
                                                PlayerData data = StorageManager.getPlayerData(target.getUUID());
                                                if (data != null) {
                                                    data.setPlayerName(target.getName().getString());
                                                    StorageManager.putPlayerData(target.getUUID(), data);
                                                }
                                                LoginManager.setAuthenticated(target, true);
                                                LoginManager.restorePlayerState(target);
                                                context.getSource().sendSuccess(() ->
                                                        Component.literal("已强制登录玩家 " + target.getName().getString()), true);
                                                return 1;
                                            } catch (Exception e) {
                                                LOGGER.error("强制登录命令执行过程中发生异常", e);
                                                context.getSource().sendFailure(Component.literal("强制登录失败，请查看服务器日志"));
                                                return 0;
                                            }
                                        })))
                        .then(Commands.literal("logout")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> {
                                            try {
                                                ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                                if (target == null) {
                                                    context.getSource().sendFailure(Component.literal("目标玩家不在线"));
                                                    return 0;
                                                }
                                                if (!LoginManager.isAuthenticated(target)) {
                                                    context.getSource().sendFailure(Component.literal("目标玩家尚未登录"));
                                                    return 0;
                                                }
                                                // 强制登出
                                                LoginManager.backupPlayerState(target);
                                                target.setGameMode(GameType.ADVENTURE);
                                                LoginManager.setAuthenticated(target, false);
                                                context.getSource().sendSuccess(() ->
                                                        Component.literal("已强制登出玩家 " + target.getName().getString()), true);
                                                return 1;
                                            } catch (Exception e) {
                                                LOGGER.error("强制登出命令执行过程中发生异常", e);
                                                context.getSource().sendFailure(Component.literal("强制登出失败，请查看服务器日志"));
                                                return 0;
                                            }
                                        })))));
    }

    private static void teleportToLastLocation(ServerPlayer player, PlayerData data) {
        if (data.getLastDimension() == null) return;

        ResourceLocation dimensionLocation = ResourceLocation.tryParse(data.getLastDimension());
        if (dimensionLocation == null) {
            LOGGER.warn("无效的维度标识符: {}", data.getLastDimension());
            return;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
        ServerLevel level = player.getServer().getLevel(dimension);
        if (level != null) {
            player.teleportTo(level, data.getLastX(), data.getLastY(), data.getLastZ(),
                    data.getLastYRot(), data.getLastXRot());
            LOGGER.info("玩家 {} 已传回上次位置", player.getName().getString());
        } else {
            LOGGER.warn("无法找到维度: {}", data.getLastDimension());
        }
    }
}