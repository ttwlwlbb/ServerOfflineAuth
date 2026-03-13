package cn.citprobe.ServerOfflineAuth.network;

import cn.citprobe.ServerOfflineAuth.ServerOfflineAuth;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ServerOfflineAuth.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++,
                TokenSyncPacket.class,
                TokenSyncPacket::encode,
                TokenSyncPacket::decode,
                TokenSyncPacket::handle);
        INSTANCE.registerMessage(packetId++,
                ClientTokenPacket.class,
                ClientTokenPacket::encode,
                ClientTokenPacket::decode,
                ClientTokenPacket::handle);
    }
}