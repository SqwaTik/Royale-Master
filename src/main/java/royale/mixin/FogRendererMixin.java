package royale.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Shadow
    private static boolean fogEnabled;

    @Inject(method = "applyFog", at = @At("HEAD"))
    private void royale$disableDistanceFog(
            Camera camera,
            int viewDistance,
            RenderTickCounter tickCounter,
            float skyDarkness,
            ClientWorld world,
            CallbackInfoReturnable<Vector4f> cir
    ) {
        fogEnabled = false;
    }
}

