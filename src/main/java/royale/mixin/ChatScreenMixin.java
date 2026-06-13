package royale.mixin;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.Initialization;
import royale.client.draggables.Drag;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
    private void onMouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (Initialization.getInstance() != null
                && Initialization.getInstance().getManager() != null
                && Initialization.getInstance().getManager().getHudManager() != null
                && Initialization.getInstance().getManager().getHudManager().mouseClicked((int) mouseX, (int) mouseY, button)) {
            cir.setReturnValue(true);
            return;
        }

        Drag.onMouseClick(click);
        if (Drag.isDragging()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"), require = 0)
    private void onRemoved(CallbackInfo ci) {
        Drag.resetDragging();
    }

    @Inject(method = "close", at = @At("HEAD"), require = 0)
    private void onClose(CallbackInfo ci) {
        Drag.resetDragging();
    }
}
