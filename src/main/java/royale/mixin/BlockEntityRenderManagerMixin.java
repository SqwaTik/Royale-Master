package royale.mixin;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.util.performance.BuiltinOptimizer;

@Mixin(BlockEntityRenderManager.class)
public class BlockEntityRenderManagerMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void royale$optimizeStorageBlockEntities(
            BlockEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraRenderState,
            CallbackInfo ci
    ) {
        if (state == null || state.type == null || state.pos == null || cameraRenderState == null || cameraRenderState.pos == null) {
            return;
        }

        double squaredDistance = state.pos.getSquaredDistance(
                cameraRenderState.pos.x,
                cameraRenderState.pos.y,
                cameraRenderState.pos.z
        );
        if (BuiltinOptimizer.shouldCullStorageBlockEntity(state.type, squaredDistance)) {
            ci.cancel();
        }
    }
}

