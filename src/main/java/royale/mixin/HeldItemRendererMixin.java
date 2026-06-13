package royale.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.events.api.EventManager;
import royale.events.api.events.Event;
import royale.events.impl.GlassHandsRenderEvent;
import royale.events.impl.HandAnimationEvent;
import royale.events.impl.HandOffsetEvent;
import royale.events.impl.HeldItemUpdateEvent;
import royale.events.impl.ItemRendererEvent;
import royale.modules.impl.render.GlassHands;
@Mixin({HeldItemRenderer.class})
public abstract class HeldItemRendererMixin
{
@Shadow
private ItemStack mainHand;
@Shadow
private ItemStack offHand;
@Unique
private boolean richCustomAnimation = false;
@Inject(method = {"updateHeldItems"}, at = {@At("TAIL")})
private void onUpdateHeldItems(CallbackInfo ci) {
HeldItemUpdateEvent event = new HeldItemUpdateEvent(this.mainHand, this.offHand);
EventManager.callEvent((Event)event);
if (event.getMainHand() != this.mainHand) {
this.mainHand = event.getMainHand();
}
if (event.getOffHand() != this.offHand) {
this.offHand = event.getOffHand();
}
}
@Inject(method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at = {@At("HEAD")})
private void onRenderItemPre(float tickProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, ClientPlayerEntity player, int light, CallbackInfo ci) {
GlassHands glassHands = GlassHands.getInstance();
if (glassHands != null && glassHands.isState()) {
GlassHandsRenderEvent event = new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.PRE, matrices, tickProgress);
EventManager.callEvent((Event)event);
} 
}
@Inject(method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at = {@At("TAIL")})
private void onRenderItemPost(float tickProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, ClientPlayerEntity player, int light, CallbackInfo ci) {
GlassHands glassHands = GlassHands.getInstance();
if (glassHands != null && glassHands.isState()) {
GlassHandsRenderEvent event = new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.POST, matrices, tickProgress);
EventManager.callEvent((Event)event);
} 
}
@WrapOperation(method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V")})
private void itemRenderHook(HeldItemRenderer instance, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, Operation<Void> original) {
ItemRendererEvent event = new ItemRendererEvent(player, item, hand);
EventManager.callEvent((Event)event);
original.call(new Object[] { instance, event.getPlayer(), Float.valueOf(tickDelta), Float.valueOf(pitch), event.getHand(), Float.valueOf(swingProgress), event.getStack(), Float.valueOf(equipProgress), matrices, orderedRenderCommandQueue, Integer.valueOf(light) });
}
@Inject(method = {"renderFirstPersonItem"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER)})
private void renderFirstPersonItemHook(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, CallbackInfo ci) {
HandOffsetEvent event = new HandOffsetEvent(matrices, stack, hand);
EventManager.callEvent((Event)event);
float scale = event.getScale();
if (scale != 1.0F) {
matrices.scale(scale, scale, scale);
}
}
@WrapOperation(method = {"renderFirstPersonItem"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V")})
private void wrapApplyEquipOffset(HeldItemRenderer instance, MatrixStack matrices, Arm arm, float equipProgress, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) AbstractClientPlayerEntity player, @Local(ordinal = 0, argsOnly = true) Hand hand, @Local(ordinal = 2, argsOnly = true) float swingProgress, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
boolean isUsingItem = (player.isUsingItem() && player.getActiveHand() == hand);
if (isUsingItem) {
this.richCustomAnimation = false;
original.call(new Object[] { instance, matrices, arm, Float.valueOf(equipProgress) });
return;
} 
HandAnimationEvent event = new HandAnimationEvent(matrices, hand, swingProgress);
EventManager.callEvent((Event)event);
if (event.isCancelled()) {
this.richCustomAnimation = true;
return;
} 
this.richCustomAnimation = false;
original.call(new Object[] { instance, matrices, arm, Float.valueOf(equipProgress) });
}
@WrapOperation(method = {"renderFirstPersonItem"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V")})
private void wrapSwingArm(HeldItemRenderer instance, float swingProgress, MatrixStack matrices, int armX, Arm arm, Operation<Void> original) {
if (this.richCustomAnimation) {
return;
}
original.call(new Object[] { instance, Float.valueOf(swingProgress), matrices, Integer.valueOf(armX), arm });
}
}


