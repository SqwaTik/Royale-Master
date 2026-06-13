package royale.mixin;

import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.modules.impl.render.Esp;
import royale.modules.impl.render.chinahat.ChinaHatFeatureRenderer;
import royale.IMinecraft;

@Mixin(value={PlayerEntityRenderer.class})
public class MixinPlayerEntityRenderer implements IMinecraft {
    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        PlayerEntityRenderer renderer = (PlayerEntityRenderer)(Object)this;
        renderer.addFeature((FeatureRenderer)new ChinaHatFeatureRenderer((FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>)renderer));
    }

    @Inject(method={"renderLabelIfPresent"}, at={@At(value="HEAD")}, cancellable=true)
    private void hideBelownameScore(PlayerEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        Esp esp = Esp.getInstance();
        if (esp != null && esp.shouldRenderCustomPlayerLabels()) {
            ci.cancel();
        }
    }

}

