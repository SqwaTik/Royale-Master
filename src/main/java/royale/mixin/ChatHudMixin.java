package royale.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.modules.impl.misc.ChatHelper;
import royale.util.chat.ChatHistoryManager;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Unique
    private boolean royale$timestampDecorating;

    @Shadow
    public abstract void addMessage(Text message);

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void onAddMessage(Text message, CallbackInfo ci) {
        if (!this.royale$timestampDecorating) {
            ChatHistoryManager.getInstance().appendIncoming(message);
        }

        if (this.royale$timestampDecorating || message == null) {
            return;
        }

        ChatHelper helper = ChatHelper.getInstance();
        if (helper == null || !helper.isState()) {
            return;
        }

        Text decorated = helper.decorateIncomingMessage(message);
        if (decorated == null || decorated == message) {
            return;
        }

        this.royale$timestampDecorating = true;
        try {
            this.addMessage(decorated);
        } finally {
            this.royale$timestampDecorating = false;
        }
        ci.cancel();
    }
}
