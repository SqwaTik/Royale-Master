package royale.mixin;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.modules.impl.render.Esp;
@Mixin({EntityRenderer.class})
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState>
{
@Inject(method = {"renderLabelIfPresent"}, at = {@At("HEAD")}, cancellable = true)
private void onRenderLabelIfPresent(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
Esp esp = Esp.getInstance();
if (esp != null && esp.isState()) {
if (((EntityRenderState)state).entityType == EntityType.PLAYER && esp.shouldRenderCustomPlayerLabels()) {
ci.cancel();
}
if (((EntityRenderState)state).entityType == EntityType.ITEM && esp.shouldRenderCustomItemLabels()) {
ci.cancel();
}
} 
}
@Inject(method = {"getDisplayName"}, at = {@At("HEAD")}, cancellable = true)
private void hookNametag(T entity, CallbackInfoReturnable<Text> cir) {
Esp esp = Esp.getInstance();
if (esp != null && esp.isState()) {
if (entity instanceof net.minecraft.entity.player.PlayerEntity && esp.shouldRenderCustomPlayerLabels()) {
cir.setReturnValue(null);
}
if (entity instanceof net.minecraft.entity.ItemEntity && esp.shouldRenderCustomItemLabels())
cir.setReturnValue(null); 
} 
}
}


