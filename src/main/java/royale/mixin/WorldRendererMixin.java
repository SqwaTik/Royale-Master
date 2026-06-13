package royale.mixin;
import net.minecraft.client.gl.DynamicUniforms;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.IMinecraft;
import royale.modules.impl.render.ChunkAnimator;
import royale.modules.impl.render.NoRender;
@Mixin({WorldRenderer.class})
public class WorldRendererMixin
implements IMinecraft
{
@ModifyArg(method = {"renderBlockLayers"}, at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), index = 0)
private Object modifyChunkSectionsValue(Object value) {
if (value instanceof DynamicUniforms.ChunkSectionsValue) { DynamicUniforms.ChunkSectionsValue original = (DynamicUniforms.ChunkSectionsValue)value;
ChunkAnimator chunkAnimator = ChunkAnimator.getInstance();
if (chunkAnimator != null && chunkAnimator.isState()) {
float visibility = original.visibility();
float animOffset = (1.0F - visibility) * 100.0F;
int newY = original.y() - (int)animOffset;
return new DynamicUniforms.ChunkSectionsValue(original
.modelView(), original
.x(), newY, original
.z(), original
.visibility(), original
.textureAtlasWidth(), original
.textureAtlasHeight());
}  }
return value;
}
@Inject(method = {"hasBlindnessOrDarkness"}, at = {@At("HEAD")}, cancellable = true)
private void onHasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> cir) {
LivingEntity livingEntity;
NoRender noRender = NoRender.getInstance();
if (noRender == null || !noRender.isState())
return; 
Entity entity = camera.getFocusedEntity();
if (entity instanceof LivingEntity) { livingEntity = (LivingEntity)entity; }
else { return; }
boolean hasBlindness = livingEntity.hasStatusEffect(StatusEffects.BLINDNESS);
boolean hasDarkness = livingEntity.hasStatusEffect(StatusEffects.DARKNESS);
if (noRender.modeSetting.isSelected("Bad Effects") && hasBlindness && !hasDarkness) {
cir.setReturnValue(Boolean.valueOf(false));
}
if (noRender.modeSetting.isSelected("Darkness") && hasDarkness && !hasBlindness) {
cir.setReturnValue(Boolean.valueOf(false));
}
if (noRender.modeSetting.isSelected("Bad Effects") && noRender.modeSetting.isSelected("Darkness"))
cir.setReturnValue(Boolean.valueOf(false)); 
}
}


