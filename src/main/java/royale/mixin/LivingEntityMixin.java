package royale.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.lang.reflect.Method;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.events.api.EventManager;
import royale.events.impl.JumpEvent;
import royale.events.impl.PushEvent;
import royale.events.impl.SwingDurationEvent;
import royale.util.angle.Angle;
import royale.util.angle.AngleConnection;
import royale.util.angle.AngleConstructor;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin {
    @Shadow
    public float bodyYaw;
    @Unique
    private final MinecraftClient client = MinecraftClient.getInstance();
    @Unique
    private static boolean baritoneChecked = false;
    @Unique
    private static boolean baritoneAvailable = false;
    @Unique
    private static Method getProviderMethod;
    @Unique
    private static Method getPrimaryBaritoneMethod;
    @Unique
    private static Method getPathingBehaviorMethod;
    @Unique
    private static Method isPathingMethod;

    @Shadow
    public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> var1);

    @Shadow
    @Nullable
    public abstract StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> var1);

    @Shadow
    public abstract boolean isInSwimmingPose();

    @Shadow
    protected abstract double getEffectiveGravity();

    @Unique
    private boolean isBaritonePathing() {
        try {
            if (!baritoneChecked) {
                baritoneChecked = true;
                try {
                    Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
                    getProviderMethod = apiClass.getMethod("getProvider", new Class[0]);
                    Class<?> providerClass = Class.forName("baritone.api.IBaritoneProvider");
                    getPrimaryBaritoneMethod = providerClass.getMethod("getPrimaryBaritone", new Class[0]);
                    Class<?> baritoneClass = Class.forName("baritone.api.IBaritone");
                    getPathingBehaviorMethod = baritoneClass.getMethod("getPathingBehavior", new Class[0]);
                    Class<?> pathingClass = Class.forName("baritone.api.behavior.IPathingBehavior");
                    isPathingMethod = pathingClass.getMethod("isPathing", new Class[0]);
                    baritoneAvailable = true;
                }
                catch (ClassNotFoundException | NoSuchMethodException e) {
                    baritoneAvailable = false;
                }
            }
            if (!baritoneAvailable) {
                return false;
            }
            Object provider = getProviderMethod.invoke(null, new Object[0]);
            if (provider == null) {
                return false;
            }
            Object baritone = getPrimaryBaritoneMethod.invoke(provider, new Object[0]);
            if (baritone == null) {
                return false;
            }
            Object pathingBehavior = getPathingBehaviorMethod.invoke(baritone, new Object[0]);
            if (pathingBehavior == null) {
                return false;
            }
            Object result = isPathingMethod.invoke(pathingBehavior, new Object[0]);
            return Boolean.TRUE.equals(result);
        }
        catch (Exception e) {
            return false;
        }
    }

    @Unique
    private boolean shouldApplyRichMoveCorrection() {
        AngleConnection rotationManager = AngleConnection.INSTANCE;
        Angle rotation = rotationManager.getRotation();
        AngleConstructor configurable = rotationManager.getCurrentRotationPlan();
        return rotation != null && configurable != null && configurable.isMoveCorrection();
    }

    @Inject(method={"isPushable"}, at={@At(value="HEAD")}, cancellable=true)
    public void isPushable(CallbackInfoReturnable<Boolean> infoReturnable) {
        PushEvent event = new PushEvent(PushEvent.Type.COLLISION);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            infoReturnable.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method={"jump"}, at={@At(value="NEW", target="(DDD)Lnet/minecraft/util/math/Vec3d;")}, require=0)
    private Vec3d hookFixRotation(Vec3d original) {
        if ((Object)this != this.client.player) {
            return original;
        }
        if (this.isBaritonePathing()) {
            return original;
        }
        if (!this.shouldApplyRichMoveCorrection()) {
            return original;
        }
        float yaw = AngleConnection.INSTANCE.getMoveRotation().getYaw() * ((float)Math.PI / 180);
        return new Vec3d((double)(-MathHelper.sin((double)yaw) * 0.2f), 0.0, (double)(MathHelper.cos((double)yaw) * 0.2f));
    }

    @Inject(method={"jump"}, at={@At(value="HEAD")}, cancellable=true)
    private void jump(CallbackInfo info) {
        Object livingEntityMixin = this;
        if (livingEntityMixin instanceof ClientPlayerEntity) {
            ClientPlayerEntity player = (ClientPlayerEntity)livingEntityMixin;
            if (this.isBaritonePathing()) {
                return;
            }
            JumpEvent event = new JumpEvent((PlayerEntity)player);
            EventManager.callEvent(event);
            if (event.isCancelled()) {
                info.cancel();
            }
        }
    }

    @Inject(method={"getHandSwingDuration"}, at={@At(value="HEAD")}, cancellable=true)
    private void swingProgressHook(CallbackInfoReturnable<Integer> cir) {
        if ((Object)this != this.client.player) {
            return;
        }
        SwingDurationEvent event = new SwingDurationEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            float animation = event.getAnimation();
            animation = StatusEffectUtil.hasHaste((LivingEntity)(Object)this.client.player) ? (animation *= (float)(6 - (1 + StatusEffectUtil.getHasteAmplifier((LivingEntity)(Object)this.client.player)))) : (animation *= (float)(this.hasStatusEffect((RegistryEntry<StatusEffect>)StatusEffects.MINING_FATIGUE) ? 6 + (1 + this.getStatusEffect((RegistryEntry<StatusEffect>)StatusEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6));
            cir.setReturnValue(((int)animation));
        }
    }

    @Inject(method={"calcGlidingVelocity"}, at={@At(value="HEAD")}, cancellable=true)
    private void calcGlidingVelocityFull(Vec3d oldVelocity, CallbackInfoReturnable<Vec3d> cir) {
        double i;
        if ((Object)this != this.client.player) {
            return;
        }
        if (this.isBaritonePathing()) {
            return;
        }
        AngleConnection rotationManager = AngleConnection.INSTANCE;
        Angle rotation = rotationManager.getRotation();
        AngleConstructor configurable = rotationManager.getCurrentRotationPlan();
        if (rotation == null || configurable == null || !configurable.isMoveCorrection() || configurable.isChangeLook()) {
            return;
        }
        Vec3d vec3d = rotation.toVector();
        float f = rotation.getPitch() * ((float)Math.PI / 180);
        double d = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
        double e = oldVelocity.horizontalLength();
        double g = this.getEffectiveGravity();
        double h = MathHelper.square((double)Math.cos(f));
        oldVelocity = oldVelocity.add(0.0, g * (-1.0 + h * 0.75), 0.0);
        if (oldVelocity.y < 0.0 && d > 0.0) {
            i = oldVelocity.y * -0.1 * h;
            oldVelocity = oldVelocity.add(vec3d.x * i / d, i, vec3d.z * i / d);
        }
        if (f < 0.0f && d > 0.0) {
            i = e * (double)(-MathHelper.sin((double)f)) * 0.04;
            oldVelocity = oldVelocity.add(-vec3d.x * i / d, i * 3.2, -vec3d.z * i / d);
        }
        if (d > 0.0) {
            oldVelocity = oldVelocity.add((vec3d.x / d * e - oldVelocity.x) * 0.1, 0.0, (vec3d.z / d * e - oldVelocity.z) * 0.1);
        }
        cir.setReturnValue(oldVelocity.multiply((double)0.99f, (double)0.98f, (double)0.99f));
        cir.cancel();
    }
}

