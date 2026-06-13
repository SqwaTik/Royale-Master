package royale.mixin;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.modules.impl.render.NoRender;
@Mixin({InGameOverlayRenderer.class})
public class InGameOverlayRendererMixin
{
@Inject(method = {"renderFireOverlay"}, at = {@At("HEAD")}, cancellable = true)
private static void renderFireOverlayHook(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Sprite sprite, CallbackInfo ci) {
NoRender noRender = NoRender.getInstance();
if (noRender.isState() && noRender.modeSetting.isSelected("Fire")) {
ci.cancel();
}
}
}


