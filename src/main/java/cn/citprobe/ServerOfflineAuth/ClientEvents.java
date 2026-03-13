package cn.citprobe.ServerOfflineAuth;

import cn.citprobe.ServerOfflineAuth.network.ClientTokenPacket;
import cn.citprobe.ServerOfflineAuth.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ServerOfflineAuth.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        // 当客户端登录到服务器时触发
        ClientTokenStorage.TokenData tokenData = ClientTokenStorage.loadToken();
        if (tokenData != null && tokenData.token != null && !tokenData.token.isEmpty()) {
            // 检查是否过期（可选，客户端也可以先过滤）
            if (tokenData.expiry == 0 || tokenData.expiry > System.currentTimeMillis()) {
                NetworkHandler.INSTANCE.sendToServer(new ClientTokenPacket(tokenData.token));
            } else {
                // token 过期，清除
                ClientTokenStorage.clearToken();
            }
        }
    }
}