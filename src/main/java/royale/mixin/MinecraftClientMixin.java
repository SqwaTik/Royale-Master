package royale.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.Initialization;
import royale.events.api.EventManager;
import royale.events.impl.GameLeftEvent;
import royale.events.impl.HotBarUpdateEvent;
import royale.events.impl.SetScreenEvent;
import royale.modules.impl.misc.Rpc;
import royale.modules.impl.render.Hud;
import royale.screens.menu.MainMenuScreen;
import royale.util.config.ConfigSystem;
import royale.util.network.ConnectionCompatController;
import royale.util.performance.BuiltinOptimizer;
import royale.util.render.font.FontRenderer;
import royale.util.rpc.DiscordRpcService;
import royale.util.session.SessionChanger;
import royale.util.window.WindowStyle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Locale;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    public ClientWorld world;

    @Shadow
    @Mutable
    private Session session;

    @Shadow
    @Final
    private UserApiService userApiService;

    @Shadow
    @Final
    @Mutable
    private ProfileKeys profileKeys;

    private static boolean fontsInitialized = false;
    private static boolean rpcCloseSignalHandled = false;

    @Inject(method = "<init>", at = @At("TAIL"), require = 0)
    private void onInit(CallbackInfo ci) {
        rpcCloseSignalHandled = false;
        safeStopRpc();
        new Initialization().init();
        SessionChanger.setSessionSetter(newSession -> this.session = newSession);
        disableProfileKeysForCustomAuth();
    }

    @Inject(method = "stop", at = @At("HEAD"), require = 0)
    private void onStop(CallbackInfo ci) {
        safeStopRpc();
        ConfigSystem configSystem = ConfigSystem.getInstance();
        if (configSystem != null) {
            configSystem.shutdown();
        }
    }

    @Inject(method = "close", at = @At("HEAD"), require = 0)
    private void onClose(CallbackInfo ci) {
        safeStopRpc();
    }

    @Inject(method = "scheduleStop", at = @At("HEAD"), require = 0)
    private void onScheduleStop(CallbackInfo ci) {
        safeStopRpc();
    }

    @Inject(method = "run", at = @At("RETURN"), require = 0)
    private void onRunEnd(CallbackInfo ci) {
        safeStopRpc();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), require = 0)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (!fontsInitialized && screen != null) {
            try {
                FontRenderer fontRenderer = Initialization.getInstance().getManager().getRenderCore().getFontRenderer();
                if (fontRenderer != null && !fontRenderer.isInitialized()) {
                    fontRenderer.initialize();
                }
                fontsInitialized = true;
            } catch (Exception ignored) {
            }
        }

        try {
            DiscordRpcService.getInstance().forceRefresh();
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true, require = 0)
    private void redirectTitleScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof TitleScreen && !(screen instanceof MainMenuScreen)) {
            ci.cancel();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.setScreen(new MainMenuScreen());
            }
        }
    }

    @Inject(method = "disconnect", at = @At("HEAD"), require = 0)
    private void onDisconnect(Text reason, CallbackInfo info) {
        if (this.world != null) {
            EventManager.callEvent(GameLeftEvent.get());
        }
    }

    @Inject(method = "disconnect", at = @At("TAIL"), require = 0)
    private void onDisconnectTail(Text reason, CallbackInfo info) {
        DiscordRpcService.getInstance().clearPendingServerAddress();
        safeStopRpc();
        ConnectionCompatController.resetAfterDisconnect();
        BuiltinOptimizer.onWorldLeft(MinecraftClient.getInstance());
    }

    @Inject(
            method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V",
            at = @At("HEAD"),
            require = 0
    )
    private void onDisconnectScreen(Screen screen, boolean transferring, CallbackInfo info) {
        if (this.world != null) {
            EventManager.callEvent(GameLeftEvent.get());
        }
    }

    @Inject(
            method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V",
            at = @At("TAIL"),
            require = 0
    )
    private void onDisconnectScreenTail(Screen screen, boolean transferring, CallbackInfo info) {
        DiscordRpcService.getInstance().clearPendingServerAddress();
        safeStopRpc();
        ConnectionCompatController.resetAfterDisconnect();
        BuiltinOptimizer.onWorldLeft(MinecraftClient.getInstance());
    }

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void onTick(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getWindow() != null && !rpcCloseSignalHandled) {
            long windowHandle = mc.getWindow().getHandle();
            if (windowHandle != 0L && GLFW.glfwWindowShouldClose(windowHandle)) {
                rpcCloseSignalHandled = true;
                safeStopRpc();
                return;
            }
        }

        tickRpcInAnyScreen();
        BuiltinOptimizer.tick(mc);
        if (mc == null || mc.player == null || mc.world == null) {
            return;
        }

        Hud hud = Hud.getInstance();
        if (hud != null
                && hud.isState()
                && Initialization.getInstance() != null
                && Initialization.getInstance().getManager() != null
                && Initialization.getInstance().getManager().getHudManager() != null) {
            Initialization.getInstance().getManager().getHudManager().tick();
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true, require = 0)
    public void setScreenHook(Screen screen, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        SetScreenEvent event = new SetScreenEvent(screen);
        EventManager.callEvent(event);
        Screen eventScreen = event.getScreen();
        if (screen != eventScreen) {
            client.setScreen(eventScreen);
            ci.cancel();
        }
    }

    @Inject(method = "getWindowTitle", at = @At("RETURN"), cancellable = true, require = 0)
    private void getWindowTitle(CallbackInfoReturnable<String> cir) {
        String username = this.session != null ? this.session.getUsername() : null;
        String fallbackNick = !isInvalidProfileValue(username) ? username : "SqwaT09";
        cir.setReturnValue(String.format("Royale Master (Free - %s)", fallbackNick));
    }

    private boolean isInvalidProfileValue(String value) {
        return value == null || value.isBlank() || "null".equalsIgnoreCase(value);
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"), cancellable = true, require = 0)
    public void handleInputEventsHook(CallbackInfo ci) {
        HotBarUpdateEvent event = new HotBarUpdateEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"), require = 0)
    private void applyDarkMode(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getWindow() != null) {
            WindowStyle.setDarkMode(client.getWindow().getHandle());
        }
    }

    private static void safeStopRpc() {
        try {
            DiscordRpcService.getInstance().stop();
        } catch (Exception ignored) {
        }
    }

    private static void tickRpcInAnyScreen() {
        try {
            if (Initialization.getInstance() == null
                    || Initialization.getInstance().getManager() == null
                    || Initialization.getInstance().getManager().getModuleProvider() == null) {
                return;
            }

            Rpc rpc = Initialization.getInstance().getManager().getModuleProvider().get(Rpc.class);
            DiscordRpcService service = DiscordRpcService.getInstance();
            if (rpc == null || !rpc.isState()) {
                if (service.isRunning()) {
                    service.stop();
                }
                return;
            }

            if (!service.isRunning()) {
                service.start();
            }
            service.tick();
        } catch (Exception ignored) {
        }
    }

    private void disableProfileKeysForCustomAuth() {
        if (!isCustomAuthEnvironment()) {
            return;
        }

        this.profileKeys = ProfileKeys.MISSING;
    }

    private boolean isCustomAuthEnvironment() {
        if (userApiService == null) {
            return false;
        }

        try {
            Field environmentField = userApiService.getClass().getDeclaredField("environment");
            environmentField.setAccessible(true);
            Object environment = environmentField.get(userApiService);
            if (environment == null) {
                return false;
            }

            String servicesHost = readEnvironmentValue(environment, "servicesHost");
            String sessionHost = readEnvironmentValue(environment, "sessionHost");
            String profilesHost = readEnvironmentValue(environment, "profilesHost");

            return isCustomAuthHost(servicesHost)
                    || isCustomAuthHost(sessionHost)
                    || isCustomAuthHost(profilesHost);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static String readEnvironmentValue(Object environment, String methodName) throws ReflectiveOperationException {
        Method method = environment.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        Object value = method.invoke(environment);
        return value instanceof String string ? string : null;
    }

    private static boolean isCustomAuthHost(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return false;
        }

        String normalized = rawUrl.toLowerCase(Locale.ROOT);
        try {
            String host = URI.create(rawUrl).getHost();
            if (host != null && !host.isBlank()) {
                normalized = host.toLowerCase(Locale.ROOT);
            }
        } catch (IllegalArgumentException ignored) {
        }

        return !normalized.contains("mojang.com")
                && !normalized.contains("minecraftservices.com");
    }
}
