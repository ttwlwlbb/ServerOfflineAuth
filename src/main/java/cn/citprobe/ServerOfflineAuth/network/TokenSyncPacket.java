package cn.citprobe.ServerOfflineAuth.network;

import cn.citprobe.ServerOfflineAuth.ClientTokenStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TokenSyncPacket {
    private final String token;
    private final long expiry;

    public TokenSyncPacket(String token, long expiry) {
        this.token = token;
        this.expiry = expiry;
    }

    public static void encode(TokenSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.token);
        buf.writeLong(packet.expiry);
    }

    public static TokenSyncPacket decode(FriendlyByteBuf buf) {
        return new TokenSyncPacket(buf.readUtf(32767), buf.readLong());
    }

    public static void handle(TokenSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTokenStorage.saveToken(packet.token, packet.expiry));
        });
        ctx.get().setPacketHandled(true);
    }
}