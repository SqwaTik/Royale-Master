package royale.mixin;

import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.util.rpc.DiscordRpcService;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(method = "close", at = @At("HEAD"), require = 0)
    private void royale$onCloseHead(CallbackInfo ci) {
        safeStopRpc();
    }

    @Inject(method = "close", at = @At("TAIL"), require = 0)
    private void royale$onCloseTail(CallbackInfo ci) {
        safeStopRpc();
    }

    private static void safeStopRpc() {
        try {
            DiscordRpcService.getInstance().stop();
        } catch (Exception ignored) {
        }
    }
}
