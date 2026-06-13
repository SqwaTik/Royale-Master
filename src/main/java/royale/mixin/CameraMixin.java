package royale.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.events.api.EventManager;
import royale.events.impl.CameraEvent;
import royale.events.impl.CameraPositionEvent;
import royale.modules.impl.combat.ShiftAnim;
import royale.util.angle.Angle;

@Mixin(value={Camera.class})
public abstract class CameraMixin {
    @Shadow
    private Vec3d pos;
    @Shadow
    @Final
    private BlockPos.Mutable blockPos;
    @Shadow
    private float yaw;
    @Shadow
    private float pitch;
    @Shadow
    private float cameraY;
    @Shadow
    private float lastCameraY;

    @Shadow
    public abstract void setRotation(float var1, float var2);

    @Shadow
    protected abstract void moveBy(float var1, float var2, float var3);

    @Shadow
    protected abstract float clipToSpace(float var1);

    @Inject(method={"update"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;setPos(DDD)V")}, cancellable=true)
    private void updateHook(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        ClientPlayerEntity player;
        if (focusedEntity instanceof ClientPlayerEntity clientPlayer) {
            ShiftAnim shiftAnim = ShiftAnim.getInstance();
            if (shiftAnim != null && shiftAnim.isActiveOnShift() && clientPlayer == net.minecraft.client.MinecraftClient.getInstance().player) {
                float eyeHeight = clientPlayer.getEyeHeight(clientPlayer.getPose());
                this.cameraY = eyeHeight;
                this.lastCameraY = eyeHeight;
            }
        }
        CameraEvent event = new CameraEvent(false, 4.0f, new Angle(this.yaw, this.pitch));
        EventManager.callEvent(event);
        Angle angle = event.getAngle();
        if (event.isCancelled() && focusedEntity instanceof ClientPlayerEntity && !(player = (ClientPlayerEntity)focusedEntity).isSleeping() && thirdPerson) {
            float pitch = inverseView ? -angle.getPitch() : angle.getPitch();
            float yaw = angle.getYaw() - (float)(inverseView ? 180 : 0);
            float distance = event.getDistance();
            this.setRotation(yaw, pitch);
            this.moveBy(event.isCameraClip() ? -distance : -this.clipToSpace(distance), 0.0f, 0.0f);
            ci.cancel();
        }
    }

    @Inject(method={"setPos(DDD)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void posHook(double x, double y, double z, CallbackInfo ci) {
        Vec3d pos = new Vec3d(x, y, z);
        CameraPositionEvent event = new CameraPositionEvent(pos);
        EventManager.callEvent(event);
        this.pos = pos = event.getPos();
        this.blockPos.set(pos.x, pos.y, pos.z);
        ci.cancel();
    }
}

