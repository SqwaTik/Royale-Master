package royale.mixin;
import java.util.WeakHashMap;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.modules.impl.render.ItemPhysic;
import royale.util.performance.BuiltinOptimizer;
@Mixin({ItemEntityRenderer.class})
public abstract class MixinItemEntityRenderer
{
@Unique
private static final WeakHashMap<ItemEntityRenderState, Boolean> groundStateMap = new WeakHashMap<>();
@Unique
private ItemEntityRenderState currentState = null;
@Inject(method = {"render"}, at = {@At("HEAD")}, cancellable = true)
private void royale$optimizeDistantItems(ItemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
if (state == null) {
return;
}
if (BuiltinOptimizer.shouldCullDistantItemEntity(state.squaredDistanceToCamera)) {
ci.cancel();
}
}
@Inject(method = {"updateRenderState"}, at = {@At("HEAD")})
private void captureGroundState(ItemEntity entity, ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
groundStateMap.put(state, Boolean.valueOf(entity.isOnGround()));
}
@Redirect(method = {"render"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
private void redirectTranslate(MatrixStack matrices, float x, float y, float z, ItemEntityRenderState state, MatrixStack matricesArg, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
this.currentState = state;
ItemPhysic itemPhysic = ItemPhysic.getInstance();
if (itemPhysic != null && itemPhysic.isState() && itemPhysic.mode.isSelected("Обычная")) {
Box box = state.itemRenderState.getModelBoundingBox();
float f = -((float)box.minY) + 0.0625F;
matrices.translate(x, f, z);
} else {
matrices.translate(x, y, z);
} 
}
@Redirect(method = {"render"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemEntityRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/ItemStackEntityRenderState;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/Box;)V"))
private void redirectRender(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, ItemStackEntityRenderState stackState, Random random, Box box) {
ItemPhysic itemPhysic = ItemPhysic.getInstance();
if (itemPhysic != null && itemPhysic.isState() && itemPhysic.mode.isSelected("Обычная") && this.currentState != null) {
float age = this.currentState.age;
float offset = this.currentState.uniqueOffset;
boolean isOnGround = ((Boolean)groundStateMap.getOrDefault(this.currentState, Boolean.valueOf(false))).booleanValue();
float rotation = ItemEntity.getRotation(age, offset);
matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotation(-rotation));
if (isOnGround) {
matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
float yOffset = (float)box.getLengthY() / 2.0F;
matrices.translate(0.0F, -yOffset + 0.0625F, 0.0F);
} else {
float spinSpeed = 15.0F;
float itemRotation = (age * spinSpeed + offset * 360.0F) % 360.0F;
matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(itemRotation));
} 
} 
ItemEntityRenderer.render(matrices, queue, light, stackState, random, box);
}
}


