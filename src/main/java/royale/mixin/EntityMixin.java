package royale.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.IMinecraft;
import royale.events.api.EventManager;
import royale.events.impl.BoundingBoxControlEvent;
import royale.events.impl.PlayerVelocityStrafeEvent;
import royale.util.angle.AngleConnection;

@Mixin(value={Entity.class})
public abstract class EntityMixin
implements IMinecraft {
    @Shadow
    private Box boundingBox;
    @Shadow
    public float yaw;

    @Inject(method={"getBoundingBox"}, at={@At(value="HEAD")}, cancellable=true)
    public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
        BoundingBoxControlEvent event = new BoundingBoxControlEvent(this.boundingBox, (Entity)(Object)this);
        EventManager.callEvent(event);
        cir.setReturnValue(event.getBox());
    }

    @Redirect(method={"updateVelocity"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d hookVelocity(Vec3d movementInput, float speed, float yaw) {
        if ((Object)this == EntityMixin.mc.player) {
            PlayerVelocityStrafeEvent event = new PlayerVelocityStrafeEvent(movementInput, speed, yaw, Entity.movementInputToVelocity((Vec3d)movementInput, (float)speed, (float)yaw));
            EventManager.callEvent(event);
            return event.getVelocity();
        }
        return Entity.movementInputToVelocity((Vec3d)movementInput, (float)speed, (float)yaw);
    }

    @ModifyVariable(method={"getRotationVector"}, at=@At(value="HEAD"), ordinal=0, argsOnly=true)
    private float modifyPitch(float pitch) {
        if ((Object)this instanceof ClientPlayerEntity && AngleConnection.INSTANCE.getCurrentAngle() != null) {
            return AngleConnection.INSTANCE.getCurrentAngle().getPitch();
        }
        return pitch;
    }
}

