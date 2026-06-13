package royale.mixin;
import net.minecraft.world.LightType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.Initialization;
import royale.modules.impl.render.FullBright;
import royale.modules.impl.render.NoRender;
@Mixin({LightmapTextureManager.class})
public class LightmapTextureManagerMixin
{
private static float royale$dynamicExposure = 120.0F;
@Redirect(method = {"update"}, at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
private float leet$getValue(Double instance) {
FullBright fullBright = (FullBright)Initialization.getInstance().getManager().getModuleProvider().get(FullBright.class);
if (fullBright != null && fullBright.isState()) {
if (fullBright.isDynamicMode()) {
return resolveDynamicBrightness(instance.floatValue());
}
return 200.0F;
} 
return instance.floatValue();
}
private float resolveDynamicBrightness(float vanillaValue) {
MinecraftClient client = MinecraftClient.getInstance();
if (client.player == null || client.world == null) {
royale$dynamicExposure = MathHelper.lerp(0.2F, royale$dynamicExposure, 140.0F);
return Math.max(vanillaValue, royale$dynamicExposure);
} 
BlockPos pos = client.player.getBlockPos();
int skyLight = client.world.getLightLevel(LightType.SKY, pos);
int blockLight = client.world.getLightLevel(LightType.BLOCK, pos);
float strongestLight = Math.max(skyLight, blockLight);
float darkness = 1.0F - strongestLight / 15.0F;
darkness = MathHelper.clamp(darkness, 0.0F, 1.0F);
float weatherPenalty = 0.0F;
if (client.world.isThundering()) {
weatherPenalty = 0.1F;
} else if (client.world.isRaining()) {
weatherPenalty = 0.06F;
} 
float adjustedDarkness = MathHelper.clamp(darkness + weatherPenalty, 0.0F, 1.0F);
float targetExposure = MathHelper.lerp(adjustedDarkness, 82.0F, 210.0F);
float adaptationSpeed = (targetExposure > royale$dynamicExposure) ? 0.28F : 0.14F;
royale$dynamicExposure = MathHelper.lerp(adaptationSpeed, royale$dynamicExposure, targetExposure);
return Math.max(vanillaValue, royale$dynamicExposure);
}
@Inject(method = {"getDarkness"}, at = {@At("HEAD")}, cancellable = true)
private void removeDarknessEffect(CallbackInfoReturnable<Float> cir) {
NoRender noRender = NoRender.getInstance();
if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Darkness"))
cir.setReturnValue(Float.valueOf(0.0F)); 
}
}


