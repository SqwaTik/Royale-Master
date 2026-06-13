package royale.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.util.theme.ClientTheme;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class PinnedServerEntryMixin extends MultiplayerServerListWidget.Entry {
    private static final String PINNED_SERVER_IP = "mc.grandix.xyz";

    @Shadow
    @Final
    private ServerInfo server;

    @Shadow
    @Final
    private MultiplayerScreen screen;

    @Inject(method = "swapEntries", at = @At("HEAD"), cancellable = true, require = 0)
    private void royale$blockPinnedSwap(int index1, int index2, CallbackInfo ci) {
        if (isPinned(this.server)) {
            ci.cancel();
            return;
        }

        ServerList serverList = this.screen.getServerList();
        if (serverList == null) {
            return;
        }

        if (isPinnedByIndex(serverList, index1) || isPinnedByIndex(serverList, index2)) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void royale$renderPinnedGlow(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (!isPinned(this.server)) {
            return;
        }

        int left = getContentX() - 1;
        int top = getContentY() - 1;
        int right = left + getContentWidth() + 2;
        int bottom = top + getContentHeight() + 2;

        int borderStrong = ClientTheme.accentWithAlpha(220);
        int borderSoft = ClientTheme.accentWithAlpha(120);
        int fill = ClientTheme.accentWithAlpha(28);

        context.fill(left + 1, top + 1, right - 1, bottom - 1, fill);

        context.fill(left, top, right, top + 1, borderStrong);
        context.fill(left, bottom - 1, right, bottom, borderStrong);
        context.fill(left, top, left + 1, bottom, borderStrong);
        context.fill(right - 1, top, right, bottom, borderStrong);

        context.fill(left + 1, top + 1, right - 1, top + 2, borderSoft);
        context.fill(left + 1, bottom - 2, right - 1, bottom - 1, borderSoft);
    }

    private boolean isPinnedByIndex(ServerList serverList, int index) {
        if (index < 0 || index >= serverList.size()) {
            return false;
        }

        return isPinned(serverList.get(index));
    }

    private boolean isPinned(ServerInfo info) {
        if (info == null || info.address == null) {
            return false;
        }

        String address = info.address.trim().toLowerCase();
        return address.equals(PINNED_SERVER_IP)
                || address.equals("mc.grandix.xyz:25565")
                || address.startsWith("mc.grandix.xyz:");
    }
}
