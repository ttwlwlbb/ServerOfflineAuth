package cn.citprobe.ServerOfflineAuth.network;

import cn.citprobe.ServerOfflineAuth.PlayerData;
import cn.citprobe.ServerOfflineAuth.StorageManager;
import cn.citprobe.ServerOfflineAuth.LoginManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientTokenPacket {
    private final String token;

    public ClientTokenPacket(String token) {
        this.token = token;
    }

    public static void encode(ClientTokenPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.token);
    }

    public static ClientTokenPacket decode(FriendlyByteBuf buf) {
        return new ClientTokenPacket(buf.readUtf(32767));
    }

    public static void handle(ClientTokenPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (LoginManager.isAuthenticated(player)) return;

            PlayerData data = StorageManager.getPlayerData(player.getUUID());
            if (data == null) return;

            if (packet.token.equals(data.getLoginToken())) {
                long now = System.currentTimeMillis();
                if (data.getTokenExpiry() == 0 || now < data.getTokenExpiry()) {
                    LoginManager.setAuthenticated(player, true);
                    LoginManager.restorePlayerState(player);
                    player.sendSystemMessage(Component.translatable("commands.login.token_success"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}