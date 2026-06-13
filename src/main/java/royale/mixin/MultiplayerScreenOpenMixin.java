package royale.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.util.config.impl.proxy.ProxyConfig;
import royale.util.proxy.GuiProxy;
import royale.util.proxy.ProxyServer;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenOpenMixin {
    private static final String PINNED_SERVER_NAME = "Grandix Universe";
    private static final String PINNED_SERVER_IP = "mc.grandix.xyz";

    private static final String[] PINNED_BLOCKED_BUTTON_KEYS = {
            "selectserver.delete",
            "selectserver.edit"
    };

    private static final String[] PINNED_BLOCKED_BUTTON_TOKENS = {
            "delete", "remove", "edit", "rename", "удалить", "изменить", "переименовать"
    };

    private static final String[] PINNED_ALLOWED_BUTTON_KEYS = {
            "selectserver.select"
    };

    private static final String[] PINNED_ALLOWED_BUTTON_TOKENS = {
            "join", "select", "connect", "войти", "подключ"
    };

    @Shadow
    protected MultiplayerServerListWidget serverListWidget;

    @Shadow
    private ServerInfo selectedEntry;

    @Shadow
    public abstract ServerList getServerList();

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    public void multiplayerGuiOpen(CallbackInfo ci) {
        MultiplayerScreen screen = (MultiplayerScreen) (Object) this;
        ensurePinnedServer();

        ProxyConfig config = ProxyConfig.getInstance();
        String buttonText = config.isProxyEnabled() && !config.getDefaultProxy().isEmpty()
                ? "\u00a7a\u041f\u0440\u043e\u043a\u0441\u0438: \u0410\u043a\u0442\u0438\u0432\u0435\u043d"
                : "\u00a77Proxy";

        ProxyServer.proxyMenuButton = ButtonWidget
                .builder(Text.literal(buttonText), button -> MinecraftClient.getInstance().setScreen(new GuiProxy(screen)))
                .dimensions(5, 5, 100, 20)
                .build();

        IScreen access = (IScreen) screen;
        access.getDrawables().add((Drawable) ProxyServer.proxyMenuButton);
        access.getSelectables().add((Selectable) ProxyServer.proxyMenuButton);
        access.getChildren().add((Element) ProxyServer.proxyMenuButton);
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void royale$updatePinnedButtons(CallbackInfo ci) {
        MultiplayerScreen screen = (MultiplayerScreen) (Object) this;
        IScreen access = (IScreen) screen;

        boolean pinnedSelected = isPinned(this.selectedEntry);
        for (Element element : access.getChildren()) {
            if (!(element instanceof ButtonWidget button)) {
                continue;
            }

            String normalizedLabel = normalizeButtonText(button);
            String normalizedKey = normalizeButtonKey(button);
            if (normalizedLabel.isEmpty() && normalizedKey.isEmpty()) {
                continue;
            }

            if (isRestrictedButton(normalizedLabel, normalizedKey)) {
                button.visible = !pinnedSelected;
                button.active = !pinnedSelected;
                continue;
            }

            if (pinnedSelected && isAllowedButton(normalizedLabel, normalizedKey)) {
                button.visible = true;
                button.active = true;
            }
        }
    }

    @Inject(method = "removeEntry", at = @At("HEAD"), cancellable = true, require = 0)
    private void royale$blockPinnedRemove(boolean confirmed, CallbackInfo ci) {
        if (isPinned(this.selectedEntry)) {
            ci.cancel();
        }
    }

    @Inject(method = "editEntry", at = @At("HEAD"), cancellable = true, require = 0)
    private void royale$blockPinnedEdit(boolean confirmed, CallbackInfo ci) {
        if (isPinned(this.selectedEntry)) {
            ci.cancel();
        }
    }

    private void ensurePinnedServer() {
        ServerList serverList = getServerList();
        if (serverList == null) {
            return;
        }

        serverList.loadFile();

        ServerInfo pinned = null;
        List<ServerInfo> duplicates = new ArrayList<>();
        for (int i = 0; i < serverList.size(); i++) {
            ServerInfo info = serverList.get(i);
            if (!isPinned(info)) {
                continue;
            }

            if (pinned == null) {
                pinned = info;
                pinned.name = PINNED_SERVER_NAME;
                pinned.address = PINNED_SERVER_IP;
            } else {
                duplicates.add(info);
            }
        }

        for (ServerInfo duplicate : duplicates) {
            serverList.remove(duplicate);
        }

        if (pinned == null) {
            pinned = new ServerInfo(PINNED_SERVER_NAME, PINNED_SERVER_IP, ServerInfo.ServerType.OTHER);
            serverList.add(pinned, false);
        }

        int pinnedIndex = indexOf(serverList, pinned);
        if (pinnedIndex < 0) {
            pinnedIndex = findPinnedIndex(serverList);
        }

        while (pinnedIndex > 0) {
            serverList.swapEntries(pinnedIndex, pinnedIndex - 1);
            pinnedIndex--;
        }

        serverList.saveFile();
        if (this.serverListWidget != null) {
            this.serverListWidget.setServers(serverList);
        }
    }

    private int indexOf(ServerList serverList, ServerInfo target) {
        if (target == null) {
            return -1;
        }

        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i) == target) {
                return i;
            }
        }

        return -1;
    }

    private int findPinnedIndex(ServerList serverList) {
        for (int i = 0; i < serverList.size(); i++) {
            if (isPinned(serverList.get(i))) {
                return i;
            }
        }

        return -1;
    }

    private String normalizeButtonText(ButtonWidget button) {
        if (button == null || button.getMessage() == null) {
            return "";
        }
        return button.getMessage().getString().toLowerCase(Locale.ROOT).trim();
    }

    private String normalizeButtonKey(ButtonWidget button) {
        if (button == null || button.getMessage() == null) {
            return "";
        }

        if (button.getMessage().getContent() instanceof TranslatableTextContent translatable) {
            return translatable.getKey().toLowerCase(Locale.ROOT).trim();
        }

        return "";
    }

    private boolean isRestrictedButton(String normalizedLabel, String normalizedKey) {
        if (isAllowedButton(normalizedLabel, normalizedKey)) {
            return false;
        }

        for (String key : PINNED_BLOCKED_BUTTON_KEYS) {
            if (normalizedKey.equals(key)) {
                return true;
            }
        }

        for (String token : PINNED_BLOCKED_BUTTON_TOKENS) {
            if (normalizedLabel.contains(token) || normalizedKey.contains(token)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAllowedButton(String normalizedLabel, String normalizedKey) {
        for (String key : PINNED_ALLOWED_BUTTON_KEYS) {
            if (normalizedKey.equals(key)) {
                return true;
            }
        }

        for (String token : PINNED_ALLOWED_BUTTON_TOKENS) {
            if (normalizedLabel.contains(token) || normalizedKey.contains(token)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPinned(ServerInfo info) {
        if (info == null || info.address == null) {
            return false;
        }

        String address = info.address.trim().toLowerCase(Locale.ROOT);
        return address.equals(PINNED_SERVER_IP)
                || address.equals("mc.grandix.xyz:25565")
                || address.startsWith("mc.grandix.xyz:");
    }
}
