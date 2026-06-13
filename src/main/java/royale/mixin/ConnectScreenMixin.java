package royale.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.modules.impl.misc.Brand;
import royale.util.network.ConnectionCompatController;
import royale.util.rpc.DiscordRpcService;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {
    @Inject(
            method = "connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;ZLnet/minecraft/client/network/CookieStorage;)V",
            at = @At("HEAD"),
            require = 0
    )
    private static void royale$connectHead(
            Screen screen,
            MinecraftClient client,
            ServerAddress address,
            ServerInfo info,
            boolean quickPlay,
            CookieStorage cookieStorage,
            CallbackInfo ci
    ) {
        royale$trackPendingServer(address, info);
    }

    @Inject(
            method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;Lnet/minecraft/client/network/CookieStorage;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void royale$instanceConnectHead(
            MinecraftClient client,
            ServerAddress address,
            ServerInfo info,
            CookieStorage cookieStorage,
            CallbackInfo ci
    ) {
        royale$trackPendingServer(address, info);
    }

    private static void royale$trackPendingServer(ServerAddress address, ServerInfo info) {
        String rawAddress = info != null && info.address != null && !info.address.isBlank()
                ? info.address
                : String.valueOf(address);
        DiscordRpcService.getInstance().setPendingServerAddress(rawAddress);
        ConnectionCompatController.prepareConnection(info);

        Brand brand = Brand.getInstance();
        if (brand != null) {
            brand.prepareConnection();
        }
    }
}
