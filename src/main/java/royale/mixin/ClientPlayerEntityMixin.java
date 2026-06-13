package royale.mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.IMinecraft;
import royale.events.api.EventManager;
import royale.events.api.events.Event;
import royale.events.impl.CloseScreenEvent;
import royale.events.impl.MoveEvent;
import royale.events.impl.PlayerTravelEvent;
import royale.events.impl.PushEvent;
import royale.events.impl.TickEvent;
import royale.events.impl.UsingItemEvent;
import royale.util.angle.AngleConnection;
import royale.util.move.MoveUtil;
@Mixin({ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin
extends AbstractClientPlayerEntity
{
@Final
@Shadow
protected MinecraftClient client;
@Shadow
public Input input;
private double prevX = 0.0D;
private double prevZ = 0.0D;
private float prevBodyYaw = 0.0F;
public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
super(world, profile);
} @Shadow
public abstract boolean isUsingItem();
@Inject(method = {"tick"}, at = {@At("HEAD")})
public void tick(CallbackInfo info) {
if (this.client.player != null && this.client.world != null) {
EventManager.callEvent((Event)new TickEvent());
}
}
@Inject(method = {"tickMovement"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick()V", shift = At.Shift.AFTER)})
private void onInputTick(CallbackInfo ci) {
if (IMinecraft.mc.player == null)
return; 
PlayerTravelEvent event = new PlayerTravelEvent(Vec3d.ZERO, false);
EventManager.callEvent((Event)event);
}
@Redirect(method = {"applyMovementSpeedFactors"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec2f;multiply(F)Lnet/minecraft/util/math/Vec2f;", ordinal = 1))
private Vec2f cancelItemSlowdown(Vec2f vec2f, float multiplier) {
UsingItemEvent event = new UsingItemEvent((byte)1);
EventManager.callEvent((Event)event);
if (event.isCancelled() && isUsingItem() && !hasVehicle()) {
return vec2f.multiply(1.0F);
}
return vec2f.multiply(multiplier);
}
@Inject(method = {"closeHandledScreen"}, at = {@At("HEAD")}, cancellable = true)
private void closeHandledScreenHook(CallbackInfo info) {
CloseScreenEvent event = new CloseScreenEvent(this.client.currentScreen);
EventManager.callEvent((Event)event);
if (event.isCancelled())
info.cancel(); 
}
@Inject(method = {"pushOutOfBlocks"}, at = {@At("HEAD")}, cancellable = true)
public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
PushEvent event = new PushEvent(PushEvent.Type.BLOCK);
EventManager.callEvent((Event)event);
if (event.isCancelled())
ci.cancel(); 
}
@ModifyVariable(method = {"move"}, at = @At("HEAD"), argsOnly = true, ordinal = 1)
private Vec3d onMoveHook(Vec3d movement) {
MoveEvent event = new MoveEvent(movement);
EventManager.callEvent((Event)event);
return event.getMovement() == null ? movement : event.getMovement();
}
@ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F")})
private float hookSilentRotationYaw(float original) {
if (IMinecraft.mc.player != null && AngleConnection.INSTANCE.getRotation() != null) {
float currentYaw = AngleConnection.INSTANCE.getRotation().getYaw();
float newBodyYaw = MoveUtil.calculateBodyYaw(currentYaw, this.prevBodyYaw, this.prevX, this.prevZ, IMinecraft.mc.player
.getX(), IMinecraft.mc.player
.getZ(), IMinecraft.mc.player.handSwingProgress);
this.prevBodyYaw = newBodyYaw;
this.prevX = IMinecraft.mc.player.getX();
this.prevZ = IMinecraft.mc.player.getZ();
IMinecraft.mc.player.setBodyYaw(newBodyYaw);
return currentYaw;
} 
return original;
}
@ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F")})
private float hookSilentRotationPitch(float original) {
if (AngleConnection.INSTANCE.getRotation() != null) {
return AngleConnection.INSTANCE.getRotation().getPitch();
}
return original;
}
}



