package royale.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.client.draggables.Drag;
import royale.events.api.EventManager;
import royale.events.impl.FovEvent;
import royale.events.impl.HotBarScrollEvent;
import royale.events.impl.KeyEvent;
import royale.events.impl.MouseRotationEvent;
import royale.screens.clickgui.ClickGui;
import royale.util.math.OptionValueUtil;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow
    private boolean cursorLocked;

    @Shadow
    private double x;

    @Shadow
    private double y;

    @Shadow
    private double cursorDeltaX;

    @Shadow
    private double cursorDeltaY;

    @Shadow
    private boolean hasResolutionChanged;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    public void onMouseButtonHook(long window, MouseInput input, int action, CallbackInfo ci) {
        if (input.button() == GLFW.GLFW_KEY_UNKNOWN || window != this.client.getWindow().getHandle()) {
            return;
        }

        EventManager.callEvent(new KeyEvent(this.client.currentScreen, InputUtil.Type.MOUSE, input.button(), action));

        if (action == GLFW.GLFW_RELEASE
                && input.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && this.client.currentScreen instanceof ChatScreen
                && Drag.isDragging()) {
            Drag.resetDragging();
        }
    }

    @Inject(
            method = "onMouseScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"),
            cancellable = true
    )
    public void onMouseScrollHook(long window, double horizontal, double vertical, CallbackInfo ci) {
        HotBarScrollEvent event = new HotBarScrollEvent(horizontal, vertical);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    private void onLockCursor(CallbackInfo ci) {
        Screen screen = this.client.currentScreen;
        if (screen instanceof ClickGui clickGui && clickGui.isClosing()) {
            this.cursorLocked = true;
            this.cursorDeltaX = 0.0D;
            this.cursorDeltaY = 0.0D;
            this.x = this.client.getWindow().getWidth() / 2.0D;
            this.y = this.client.getWindow().getHeight() / 2.0D;
            this.hasResolutionChanged = true;
            ci.cancel();
        }
    }

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
        FovEvent event = new FovEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            int baseFov = getCurrentFovInt();
            if (baseFov <= 0) {
                return;
            }
            double slowdown = event.getFov() / (double) baseFov;
            this.cursorDeltaX *= slowdown;
            this.cursorDeltaY *= slowdown;
        }
    }

    @WrapWithCondition(
            method = "updateMouse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"),
            require = 0,
            allow = 1
    )
    private boolean modifyMouseRotationInput(ClientPlayerEntity instance, double cursorDeltaX, double cursorDeltaY) {
        MouseRotationEvent event = new MouseRotationEvent((float) cursorDeltaX, (float) cursorDeltaY);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        instance.changeLookDirection(event.getCursorDeltaX(), event.getCursorDeltaY());
        return false;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Screen screen = this.client.currentScreen;
        if (screen instanceof ClickGui clickGui && clickGui.isClosing() && !this.cursorLocked) {
            this.cursorLocked = true;
            this.cursorDeltaX = 0.0D;
            this.cursorDeltaY = 0.0D;
            this.hasResolutionChanged = true;
        }
    }

    private int getCurrentFovInt() {
        if (this.client == null || this.client.options == null) {
            return 70;
        }
        return Math.max(2, OptionValueUtil.toInt(this.client.options.getFov().getValue(), 70));
    }
}
