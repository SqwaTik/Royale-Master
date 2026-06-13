package royale.mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.IMinecraft;
import royale.events.api.EventManager;
import royale.events.api.events.Event;
import royale.events.impl.EntityColorEvent;
import royale.util.angle.AngleConnection;
import royale.util.performance.BuiltinOptimizer;
@Mixin({LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
implements IMinecraft
{
@Shadow
@Nullable
protected abstract RenderLayer getRenderLayer(S paramS, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3);
@Inject(method = {"render"}, at = {@At("HEAD")}, cancellable = true)
private void royale$optimizeDistantPlayers(S renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
if (!(renderState instanceof PlayerEntityRenderState state) || mc == null || mc.player == null) {
return;
}
if (state.id == mc.player.getId()) {
return;
}
if (BuiltinOptimizer.shouldCullDistantPlayer(state.squaredDistanceToCamera)) {
ci.cancel();
}
}
@ModifyExpressionValue(method = {"updateRenderState"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F")})
private float lerpAngleDegreesHook(float original, @Local(ordinal = 0, argsOnly = true) LivingEntity entity, @Local(ordinal = 0, argsOnly = true) float delta) {
AngleConnection controller = AngleConnection.INSTANCE;
if (entity.equals(mc.player) && controller.getCurrentAngle() != null && !(mc.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen)) {
float prevYaw = controller.getPreviousRotation().getYaw();
float currentYaw = controller.getRotation().getYaw();
return MathHelper.lerpAngleDegrees(delta, prevYaw, currentYaw);
} 
return original;
}
@ModifyExpressionValue(method = {"updateRenderState"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F")})
private float getLerpedPitchHook(float original, @Local(ordinal = 0, argsOnly = true) LivingEntity entity, @Local(ordinal = 0, argsOnly = true) float delta) {
AngleConnection controller = AngleConnection.INSTANCE;
if (entity.equals(mc.player) && controller.getCurrentAngle() != null && !(mc.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen)) {
float prevPitch = controller.getPreviousRotation().getPitch();
float currentPitch = controller.getRotation().getPitch();
return MathHelper.lerp(delta, prevPitch, currentPitch);
} 
return original;
}
@Redirect(method = {"render"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;"))
private RenderLayer renderLayerHook(LivingEntityRenderer<?, ?, ?> instance, LivingEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
if (!translucent && state.width == 0.6F) {
EntityColorEvent event = new EntityColorEvent(-1);
EventManager.callEvent((Event)event);
if (event.isCancelled()) {
translucent = true;
}
} 
return getRenderLayer((S)state, showBody, translucent, showOutline);
}
@ModifyArg(method = {"render"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V"), index = 6)
private int modifyColor(int color, @Local(argsOnly = true) S renderState) {
if (((LivingEntityRenderState)renderState).invisibleToPlayer) {
EntityColorEvent event = new EntityColorEvent(color);
EventManager.callEvent((Event)event);
return event.getColor();
} 
return color;
}
}


