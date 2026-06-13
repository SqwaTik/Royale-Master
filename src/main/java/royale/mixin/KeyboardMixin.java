package royale.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.events.api.EventManager;
import royale.events.impl.KeyEvent;
import royale.screens.clickgui.ClickGui;
import royale.util.config.impl.bind.BindConfig;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Final
    @Shadow
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (input.key() == GLFW.GLFW_KEY_UNKNOWN || window != this.client.getWindow().getHandle()) {
            return;
        }

        if (action == GLFW.GLFW_PRESS
                && input.key() == BindConfig.getInstance().getBindKey()
                && (input.modifiers() & GLFW.GLFW_MOD_ALT) == 0
                && canOpenClickGui()) {
            ClickGui.INSTANCE.openGui();
        }

        EventManager.callEvent(new KeyEvent(this.client.currentScreen, InputUtil.Type.KEYSYM, input.key(), action));
    }

    private boolean canOpenClickGui() {
        if (this.client.world == null || this.client.player == null) {
            return false;
        }
        return this.client.currentScreen == null;
    }
}
