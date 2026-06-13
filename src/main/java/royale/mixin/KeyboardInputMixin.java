package royale.mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import royale.IMinecraft;
import royale.events.api.EventManager;
import royale.events.api.events.Event;
import royale.events.impl.InputEvent;
import royale.util.angle.Angle;
import royale.util.angle.AngleConnection;
import royale.util.angle.AngleConstructor;
@Mixin({KeyboardInput.class})
public class KeyboardInputMixin
{
@ModifyExpressionValue(method = {"tick"}, at = {@At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/util/PlayerInput;")})
private PlayerInput tickHook(PlayerInput original) {
InputEvent event = new InputEvent(original);
EventManager.callEvent((Event)event);
return transformInput(event.getInput());
}
@Unique
private PlayerInput transformInput(PlayerInput input) {
AngleConnection rotationController = AngleConnection.INSTANCE;
Angle angle = rotationController.getCurrentAngle();
AngleConstructor configurable = rotationController.getCurrentRotationPlan();
if (IMinecraft.mc.player == null || angle == null || configurable == null || 
!configurable.isMoveCorrection() || !configurable.isFreeCorrection()) {
return input;
}
float deltaYaw = IMinecraft.mc.player.getYaw() - angle.getYaw();
float z = KeyboardInput.getMovementMultiplier(input.forward(), input.backward());
float x = KeyboardInput.getMovementMultiplier(input.left(), input.right());
float newX = x * MathHelper.cos((deltaYaw * 0.017453292F)) - z * MathHelper.sin((deltaYaw * 0.017453292F));
float newZ = z * MathHelper.cos((deltaYaw * 0.017453292F)) + x * MathHelper.sin((deltaYaw * 0.017453292F));
int movementSideways = Math.round(newX);
int movementForward = Math.round(newZ);
return new PlayerInput((movementForward > 0.0F), (movementForward < 0.0F), (movementSideways > 0.0F), (movementSideways < 0.0F), input
.jump(), input
.sneak(), input
.sprint());
}
}


