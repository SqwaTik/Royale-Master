package royale.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.IMinecraft;
import royale.modules.impl.combat.Gazan67;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin implements IMinecraft {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightSleeve;
    @Shadow @Final public ModelPart leftSleeve;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void royale$applyGazan67ThirdPerson(PlayerEntityRenderState state, CallbackInfo ci) {
        Gazan67 gazan67 = Gazan67.getInstance();
        if (gazan67 == null || !gazan67.isState() || mc == null || mc.player == null || state.id != mc.player.getId()) {
            return;
        }

        applyArm(this.rightArm, this.rightSleeve, gazan67.getLift(Hand.MAIN_HAND), true);
        applyArm(this.leftArm, this.leftSleeve, gazan67.getLift(Hand.OFF_HAND), false);
    }

    private void applyArm(ModelPart arm, ModelPart sleeve, float lift, boolean right) {
        float side = right ? -1.0F : 1.0F;
        arm.pitch = (float) Math.toRadians(-8.0F - 128.0F * lift);
        arm.yaw = side * (float) Math.toRadians(8.0F + 10.0F * lift);
        arm.roll = side * (float) Math.toRadians(5.0F + 34.0F * lift);
        sleeve.pitch = arm.pitch;
        sleeve.yaw = arm.yaw;
        sleeve.roll = arm.roll;
        sleeve.originX = arm.originX;
        sleeve.originY = arm.originY;
        sleeve.originZ = arm.originZ;
    }
}