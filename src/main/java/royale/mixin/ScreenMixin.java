package royale.mixin;

import net.minecraft.text.ClickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.command.CommandManager;
import royale.screens.clickgui.ClickGui;

@Mixin(value={Screen.class})
public class ScreenMixin {
    @Inject(method={"renderBackground"}, at={@At(value="HEAD")}, cancellable=true)
    private void disableBackgroundBlurAndDimming(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object)this instanceof ClickGui) {
            ci.cancel();
        }
    }

    @Inject(method={"handleClickEvent"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onHandleClickEvent(ClickEvent clickEvent, MinecraftClient client, Screen screenAfterRun, CallbackInfo ci) {
        if (clickEvent instanceof ClickEvent.RunCommand) {
            ClickEvent.RunCommand runCommand = (ClickEvent.RunCommand)clickEvent;
            String command = runCommand.command();
            CommandManager manager = CommandManager.getInstance();
            if (manager != null && command != null && command.startsWith(manager.getPrefix())) {
                manager.execute(command.substring(manager.getPrefix().length()));
                ci.cancel();
            }
        }
    }
}

