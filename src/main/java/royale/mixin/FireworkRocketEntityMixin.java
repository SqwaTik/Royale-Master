package royale.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import royale.IMinecraft;
import royale.events.api.EventManager;
import royale.events.api.events.Event;
import royale.events.impl.FireworkEvent;
import royale.util.angle.AngleConnection;
@Mixin({FireworkRocketEntity.class})
public class FireworkRocketEntityMixin
implements IMinecraft
{
@WrapOperation(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;")})
public Vec3d getRotationVectorHook(LivingEntity instance, Operation<Vec3d> original) {
if (this.shooter == mc.player && this.shooter.isGliding()) {
return AngleConnection.INSTANCE.getMoveRotation().toVector();
}
return (Vec3d)original.call(new Object[] { instance });
} @Shadow
@Nullable
private LivingEntity shooter; @WrapOperation(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V")})
public void setVelocityHook(LivingEntity instance, Vec3d velocity, Operation<Void> original) {
if (this.shooter == mc.player && this.shooter.isGliding()) {
FireworkEvent event = new FireworkEvent(velocity);
EventManager.callEvent((Event)event);
original.call(new Object[] { instance, event.getVector() });
} else {
original.call(new Object[] { instance, velocity });
} 
}
}


